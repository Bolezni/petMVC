package com.example.demo.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthenticationRequest(
        @Email(message = "Неверный формат email")
        @NotBlank(message = "Email не может быть пустым")
        String email,

        @NotBlank(message = "Пароль не введен")
        @Size(min = 6, max = 255, message = "Длина пароля должна быть от 6 до 255 символов")
        String password
) {

}
