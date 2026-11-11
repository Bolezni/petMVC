package com.example.demo.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProgramItemRequest(
        @NotNull(message = "Укажите упражнение")
        Long exerciseId,

        @Min(value = 1, message = "Подходов минимум 1")
        @Max(value = 100, message = "Подходов максимум 100")
        Integer sets,

        @Min(value = 1, message = "Повторений минимум 1")
        @Max(value = 1000, message = "Повторений максимум 1000")
        Integer reps,

        @Size(max = 1000, message = "Комментарий не длиннее 1000 символов")
        String note
) {}
