package com.example.demo.api.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "refreshToken обязателен")
        String refreshToken
) {
}
