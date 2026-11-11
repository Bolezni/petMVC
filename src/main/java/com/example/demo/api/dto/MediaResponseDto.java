package com.example.demo.api.dto;

import com.example.demo.store.entities.TypeMedia;

import java.time.LocalDateTime;

public record MediaResponseDto(
        Long id,
        TypeMedia type,
        String originalName,
        long sizeBytes,
        LocalDateTime createdAt
) {
}
