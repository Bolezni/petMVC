package com.example.demo.api.service.media;

import com.example.demo.api.dto.MediaContent;
import com.example.demo.api.dto.MediaResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {
    /** Загружает файл от текущего пользователя (для демонстраций это специалист). */
    MediaResponseDto upload(MultipartFile file);

    /**
     * @param signedAccess true — доступ уже подтверждён подписью в ссылке,
     *                     проверять пользователя не нужно.
     */
    MediaContent getContent(Long id, boolean signedAccess);

    /** Удаляет файл из хранилища и запись из БД. Только владелец. */
    void delete(Long id);
}
