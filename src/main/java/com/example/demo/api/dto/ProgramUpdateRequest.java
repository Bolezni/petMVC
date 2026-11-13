package com.example.demo.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Полное обновление программы: состав упражнений заменяется целиком.
 * Пользователь и специалист не меняются — они уже зафиксированы в программе.
 */
public record ProgramUpdateRequest(
        @NotBlank(message = "Название не может быть пустым")
        @Size(max = 255)
        String title,

        @Size(max = 5000, message = "Комментарий не длиннее 5000 символов")
        String specialistComment,

        @Size(max = 10, message = "Не больше 10 приоритетов")
        List<@NotBlank @Size(max = 100) String> priorities,

        @NotEmpty(message = "Добавьте хотя бы одно упражнение")
        @Size(max = 30, message = "Не больше 30 упражнений")
        List<@Valid ProgramItemRequest> items
) {
}
