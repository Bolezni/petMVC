package com.example.demo.api.controller;

import com.example.demo.api.dto.MediaContent;
import com.example.demo.api.dto.MediaResponseDto;
import com.example.demo.api.service.media.MediaService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/profile/avatar")
@RequiredArgsConstructor
public class AvatarController {

    private final MediaService mediaService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaResponseDto> upload(@RequestPart("file") MultipartFile file) {
        try {
            MediaResponseDto dto = mediaService.setAvatar(file);
            return ResponseEntity.ok(dto);
        }catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete() {
        mediaService.removeAvatar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> get(@PathVariable Long id) {
        MediaContent content = mediaService.getContent(id, false);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.contentType()))
                .body(content.resource());
    }
}