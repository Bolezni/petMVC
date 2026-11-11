package com.example.demo.api.dto;

import com.example.demo.store.entities.ProgramEntity;
import com.example.demo.store.entities.ProgramStatus;

import java.util.List;

public record ProgramResponse(
        Long id,
        Long userId,
        Long specialistId,
        String title,
        ProgramStatus status,
        String specialistComment,
        List<String> priorities,
        List<ProgramItemResponse> items
) {
    public static ProgramResponse from(ProgramEntity p) {
        return new ProgramResponse(p.getId(), p.getUser().getId(), p.getSpecialist().getId(),
                p.getTitle(), p.getStatus(), p.getSpecialistComment(), p.getPriorities(),
                p.getItems().stream().map(ProgramItemResponse::from).toList());
    }
}