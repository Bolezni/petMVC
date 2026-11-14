package com.example.demo.api.service.media;

import com.example.demo.api.dto.MediaContent;
import com.example.demo.api.dto.MediaResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {
    /** Загружает файл от текущего пользователя (для демонстраций это специалист). */
    MediaResponseDto upload(MultipartFile file);

    MediaContent getContent(Long id);

    /** Удаляет файл из хранилища и запись из БД. Только владелец. */
    void delete(Long id);
}
