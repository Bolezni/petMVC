package com.example.demo.api.controller;

import com.example.demo.api.dto.MediaContent;
import com.example.demo.api.dto.MediaResponseDto;
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

    /**
     * Загрузка медиа доступна только специалисту: это видео-демонстрации упражнений.
     * Пользовательских загрузок в системе нет.
     */
    @PostMapping(value = "/specialist/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MediaResponseDto upload(@RequestParam("file") MultipartFile file) {
        return mediaService.upload(file);
    }

    @GetMapping("/media/{id}/content")
    public ResponseEntity<Resource> content(@PathVariable Long id) {
        MediaContent c = mediaService.getContent(id);
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
