package com.example.demo.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
        @NotBlank String currentPassword,

        @NotBlank(message = "Пароль не введен")
        @Size(min = 6, max = 255, message = "Длина пароля должна быть от 6 до 255 символов")
        String newPassword,

        @NotBlank(message = "Повторный пароль не введен")
        String repeatNewPassword
) {
        @AssertTrue(message = "Пароли не совпадают")
        public boolean isPasswordsMatch() {
                return newPassword != null && newPassword.equals(repeatNewPassword);
        }
}
