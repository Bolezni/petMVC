package com.example.demo.api.controller;

import com.example.demo.api.dto.MediaContent;
import com.example.demo.api.dto.MediaResponseDto;
import com.example.demo.api.security.MediaUrlSigner;
import com.example.demo.api.service.media.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;
    private final MediaUrlSigner mediaUrlSigner;

    /**
     * Загрузка медиа доступна только специалисту: это видео-демонстрации упражнений.
     * Пользовательских загрузок в системе нет.
     */
    @PostMapping(value = "/specialist/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MediaResponseDto upload(@RequestParam("file") MultipartFile file) {
        return mediaService.upload(file);
    }

    /**
     * Отдача файла. Путь открыт в security-конфиге, поэтому доступ проверяем здесь:
     * либо валидная подпись в ссылке, либо авторизованный пользователь по правилам сервиса.
     * Подпись нужна для <img>/<video>: они не умеют отправлять Authorization.
     */
    @GetMapping("/media/{id}/content")
    public ResponseEntity<Resource> content(@PathVariable Long id,
                                            @RequestParam(required = false) Long exp,
                                            @RequestParam(required = false) String sig) {
        boolean signed = mediaUrlSigner.isValid(id, exp, sig);

        MediaContent c = mediaService.getContent(id, signed);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(c.contentType()))
                .body(c.resource());
    }

    /** Удалить свой файл. */
    @DeleteMapping("/media/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        mediaService.delete(id);
    }
}
