package com.example.demo.api.service.media;

import com.example.demo.api.dto.MediaContent;
import com.example.demo.api.dto.MediaResponseDto;
import com.example.demo.api.exception.exeptions.NotFoundException;
import com.example.demo.api.security.CheckAuthUtil;
import com.example.demo.store.entities.MediaFileEntity;
import com.example.demo.store.entities.Roles;
import com.example.demo.store.entities.TypeMedia;
import com.example.demo.store.entities.UserEntity;
import com.example.demo.store.repo.ExerciseRepository;
import com.example.demo.store.repo.MediaFileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private static final Map<String, String> PHOTO_TYPES =
            Map.of("image/jpeg", "jpg", "image/png", "png", "image/webp", "webp");
    private static final Map<String, String> VIDEO_TYPES =
            Map.of("video/mp4", "mp4", "video/quicktime", "mov");

    private static final long MAX_PHOTO = 10L * 1024 * 1024;
    private static final long MAX_VIDEO = 50L * 1024 * 1024;

    private final MediaFileRepository repository;
    private final ExerciseRepository exerciseRepository;
    private final FileStorage storage;

    @Transactional
    @Override
    public MediaResponseDto upload(MultipartFile file) {
        UserEntity user = CheckAuthUtil.getAuthUser();

        if (file.isEmpty()) throw new RuntimeException("Файл пустой");

        String contentType = file.getContentType();
        TypeMedia kind;
        String ext;
        long maxSize;

        if (contentType != null && PHOTO_TYPES.containsKey(contentType)) {
            kind = TypeMedia.PHOTO; ext = PHOTO_TYPES.get(contentType); maxSize = MAX_PHOTO;
        } else if (contentType != null && VIDEO_TYPES.containsKey(contentType)) {
            kind = TypeMedia.VIDEO; ext = VIDEO_TYPES.get(contentType); maxSize = MAX_VIDEO;
        } else {
            throw new RuntimeException("Неподдерживаемый тип файла");
        }

        if (file.getSize() > maxSize) throw new RuntimeException("Файл слишком большой");

        try {
            if (!matchesSignature(file, ext)) {
                throw new RuntimeException("Содержимое файла не соответствует его типу");
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать файл");
        }

        // имя на диске генерируем сами, имя от клиента в путь не попадает
        String key = user.getId() + "/" + UUID.randomUUID() + "." + ext;

        try (InputStream in = file.getInputStream()) {
            storage.save(key, in);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить файл");
        }

        try {
            MediaFileEntity entity = new MediaFileEntity();
            entity.setUser(user);
            entity.setType(kind);
            entity.setStorageKey(key);
            entity.setOriginalName(file.getOriginalFilename());
            entity.setContentType(contentType);
            entity.setSizeBytes(file.getSize());
            return toDto(repository.saveAndFlush(entity));
        } catch (RuntimeException e) {
            storage.delete(key);                   // не оставляем файл-сироту
            throw e;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public MediaContent getContent(Long id, boolean signedAccess) {
        MediaFileEntity media = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Файл не найден"));

        if (!signedAccess && !hasAccess(media)) {
            throw new NotFoundException("Файл не найден");   // 404, а не 403: не раскрываем, что файл есть
        }

        Resource resource = storage.load(media.getStorageKey());
        if (!resource.exists()) throw new NotFoundException("Файл не найден");

        return new MediaContent(resource, media.getContentType());
    }

    /** Кто может смотреть файл без подписи: владелец, специалист, видео упражнения. */
    private boolean hasAccess(MediaFileEntity media) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }

        UserEntity current = CheckAuthUtil.getAuthUser();
        boolean owner = media.getUser().getId().equals(current.getId());
        boolean specialist = current.getRole() == Roles.SPECIALIST;
        // видео-демонстрация упражнения — часть контента, её видят все авторизованные
        boolean exerciseVideo = exerciseRepository.existsByVideoMediaIdAndActiveTrue(media.getId());

        return owner || specialist || exerciseVideo;
    }

    @Transactional
    @Override
    public void delete(Long id) {
        MediaFileEntity media = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Файл не найден"));

        UserEntity current = CheckAuthUtil.getAuthUser();
        boolean owner = media.getUser().getId().equals(current.getId());
        if (!owner) {
            throw new NotFoundException("Файл не найден");   // удалять может только владелец
        }

        // сначала фиксируем удаление записи: если БД упадёт, файл останется целым
        repository.delete(media);
        repository.flush();

        // файл удаляем best-effort: осиротевший файл безвреден, битая запись — нет
        storage.delete(media.getStorageKey());
    }

    // клиентский Content-Type легко подделать, проверяем первые байты
    private boolean matchesSignature(MultipartFile file, String ext) throws IOException {
        byte[] h = new byte[12];
        try (InputStream in = file.getInputStream()) {
            if (in.readNBytes(h, 0, 12) < 12) return false;
        }
        return switch (ext) {
            case "jpg"  -> (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8;
            case "png"  -> (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G';
            case "webp" -> h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                    && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P';
            case "mp4", "mov" -> h[4] == 'f' && h[5] == 't' && h[6] == 'y' && h[7] == 'p';
            default -> false;
        };
    }

    private MediaResponseDto toDto(MediaFileEntity m) {
        return new MediaResponseDto(m.getId(), m.getType(), m.getOriginalName(),
                m.getSizeBytes(), m.getCreatedAt());
    }
}
