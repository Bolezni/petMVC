package com.example.demo.api.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record QuestionnaireResponseDto(
        Long id,
        Long userId,
        int schemaVersion,
        Map<String, Object> data,
        LocalDateTime createdAt
) {
}
