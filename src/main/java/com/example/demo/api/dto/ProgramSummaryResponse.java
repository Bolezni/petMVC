package com.example.demo.api.dto;

import com.example.demo.store.entities.ProgramEntity;
import com.example.demo.store.entities.ProgramStatus;

public record ProgramSummaryResponse(Long id, String title, ProgramStatus status) {
    public static ProgramSummaryResponse from(ProgramEntity p) {
        return new ProgramSummaryResponse(p.getId(), p.getTitle(), p.getStatus());
    }
}
