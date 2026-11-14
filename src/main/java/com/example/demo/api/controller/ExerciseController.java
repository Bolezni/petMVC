package com.example.demo.api.controller;

import com.example.demo.api.dto.ExerciseRequest;
import com.example.demo.api.dto.ExerciseResponse;
import com.example.demo.api.dto.MediaResponseDto;
import com.example.demo.api.dto.PageResponse;
import com.example.demo.api.service.exercise.ExerciseService;
import com.example.demo.api.service.media.MediaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/specialist/exercises")
@RequiredArgsConstructor
@Validated
public class ExerciseController {

    private final ExerciseService exerciseService;
    private final MediaService mediaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExerciseResponse create(@Valid @RequestBody ExerciseRequest request) {
        return exerciseService.create(request);
    }

    @PatchMapping("/{id}")
    public ExerciseResponse update(@PathVariable Long id, @Valid @RequestBody ExerciseRequest request) {
        return exerciseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        exerciseService.deactivate(id);
    }

    @GetMapping("/{id}")
    public ExerciseResponse get(@PathVariable Long id) {
        return exerciseService.get(id);
    }

    @GetMapping
    public PageResponse<ExerciseResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String muscleGroup,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return exerciseService.search(q, muscleGroup, page, size);
    }

    /** Загрузить видео-демонстрацию и привязать её к упражнению. */
    @PostMapping(value = "/{id}/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ExerciseResponse uploadVideo(@PathVariable Long id,
                                        @RequestParam("file") MultipartFile file) {
        MediaResponseDto media = mediaService.upload(file);
        return exerciseService.attachVideo(id, media.id());
    }

    /** Отвязать видео и удалить файл. */
    @DeleteMapping("/{id}/video")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeVideo(@PathVariable Long id) {
        ExerciseResponse current = exerciseService.get(id);
        exerciseService.detachVideo(id);

        if (current.videoMediaId() != null) {
            try {
                mediaService.delete(current.videoMediaId());
            } catch (RuntimeException ex) {
                log.warn("Видео {} отвязано, но файл удалить не удалось: {}",
                        current.videoMediaId(), ex.getMessage());
            }
        }
    }
}
