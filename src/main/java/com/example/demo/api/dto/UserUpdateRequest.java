package com.example.demo.api.dto;

import com.example.demo.store.entities.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Входной DTO обновления профиля.
 * Сознательно НЕ содержит id, role, password, telegramId — защита от mass assignment.
 */
public record UserUpdateRequest(
        @NotBlank(message = "Имя обязательно") String name,
        @NotBlank(message = "Фамилия обязательна") String surname,
        @Email @NotBlank(message = "Email обязателен") String email,
        @NotBlank(message = "Телефон обязателен") String phone,
        @NotNull(message = "Пол обязателен") Gender gender,
        LocalDate birthday
) {
}
