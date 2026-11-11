package com.example.demo.api.dto;

import com.example.demo.store.entities.Difficulty;
import com.example.demo.store.entities.ExerciseEntity;

public record ExerciseResponse(
        Long id,
        String name,
        String description,
        String muscleGroup,
        Difficulty difficulty,
        String videoUrl
) {
    public static ExerciseResponse from(ExerciseEntity e) {
        return new ExerciseResponse(e.getId(), e.getName(), e.getDescription(),
                e.getMuscleGroup(), e.getDifficulty(), e.getVideoUrl());
    }
}
