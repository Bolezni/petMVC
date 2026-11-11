package com.example.demo.api.dto;

import com.example.demo.store.entities.Gender;
import jakarta.validation.constraints.*;
import org.jspecify.annotations.NonNull;

import java.time.LocalDate;

public record RegisterRequest(
        @Email(message = "Не верно введен Email")
        @NotBlank(message = "Email не может быть пустым")
        String email,

        @NotBlank(message = "Пароль не введен")
        @Size(min = 6, max = 255, message = "Длина пароля должна быть от 6 до 255 символов")

        String password,

        @NotBlank(message = "Пароль не введен")
        @Size(min = 6, max = 255, message = "Длина пароля должна быть от 6 до 255 символов")
        String passwordRepeat,

        @NotBlank(message = "Телефон обязателен")
        @Pattern(regexp = "^\\+7\\d{10}$", message = "Некорректный формат телефона")
        String phone,

        @Size(max = 100, message = "Имя слишком длинное")
        String name,

        @Size(max = 100, message = "Фамилия слишком длинная")
        String surname,

        @NotNull(message = "Пол должен быть указан")
        Gender gender,

        @Past(message = "Дата рождения должна быть в прошлом")
        LocalDate birthday
) {
}
