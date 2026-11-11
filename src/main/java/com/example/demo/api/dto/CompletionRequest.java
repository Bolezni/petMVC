package com.example.demo.api.dto;

import jakarta.validation.constraints.Size;

public record CompletionRequest(
        @Size(max = 1000, message = "Комментарий не длиннее 1000 символов")
        String feedback
) {
}
