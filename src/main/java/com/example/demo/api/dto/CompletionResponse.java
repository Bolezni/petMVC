package com.example.demo.api.dto;

import com.example.demo.store.entities.ProgramCompletionEntity;
import com.example.demo.store.entities.ProgramItemEntity;

import java.time.LocalDateTime;

public record CompletionResponse(
        Long id,
        Long programItemId,
        String exerciseName,
        String feedback,
        LocalDateTime completedAt
) {
    public static CompletionResponse from(ProgramCompletionEntity c) {
        ProgramItemEntity item = c.getProgramItem();
        return new CompletionResponse(c.getId(), item.getId(),
                item.getExercise().getName(), c.getFeedback(), c.getCreatedAt());
    }
}