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

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @GetMapping("/specialist/users/{userId}/media")
    public List<MediaResponseDto> userMedia(@PathVariable Long userId) {
        return mediaService.getByUser(userId);
    }

    @GetMapping("/media/{id}/content")
    public ResponseEntity<Resource> content(@PathVariable Long id) {
        MediaContent c = mediaService.getContent(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(c.contentType()))
                .body(c.resource());
    }

    /** Удалить свой файл. Специалист удалять чужие материалы не может. */
    @DeleteMapping("/media/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        mediaService.delete(id);
    }
}
