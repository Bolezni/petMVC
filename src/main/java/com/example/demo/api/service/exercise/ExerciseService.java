package com.example.demo.api.service.exercise;

import com.example.demo.api.dto.ExerciseRequest;
import com.example.demo.api.dto.ExerciseResponse;
import com.example.demo.api.dto.PageResponse;

public interface ExerciseService {
    ExerciseResponse create(ExerciseRequest request);

    ExerciseResponse update(Long id, ExerciseRequest request);

    void deactivate(Long id);

    ExerciseResponse get(Long id);

    PageResponse<ExerciseResponse> search(String q, String muscleGroup, int page, int size);

    /** Привязать загруженный видеофайл к упражнению. */
    ExerciseResponse attachVideo(Long exerciseId, Long mediaId);

    /** Отвязать видео; сам файл удаляется отдельно. */
    ExerciseResponse detachVideo(Long exerciseId);
}
