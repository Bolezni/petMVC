package com.example.demo.api.dto;

import java.time.LocalDateTime;

public record UserListItemDto(
        Long id,
        String email,
        String phone,
        LocalDateTime lastQuestionnaireAt
) {
}
