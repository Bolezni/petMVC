package com.example.demo.api.dto;

import com.example.demo.store.entities.ExerciseEntity;
import com.example.demo.store.entities.ProgramItemEntity;

public record ProgramItemResponse(
        Long id,
        Long exerciseId,
        String exerciseName,
        String description,
        String muscleGroup,
        String videoUrl,
        Long videoMediaId,
        Integer sets,
        Integer reps,
        String note,
        Integer sortOrder
) {
    public static ProgramItemResponse from(ProgramItemEntity i) {
        ExerciseEntity e = i.getExercise();
        return new ProgramItemResponse(i.getId(), e.getId(), e.getName(), e.getDescription(),
                e.getMuscleGroup(), e.getVideoUrl(), e.getVideoMediaId(),
                i.getSets(), i.getReps(), i.getSpecialistNote(), i.getSortOrder());
    }
}
