package com.example.demo.api.dto;

import com.example.demo.api.annotation.MaxAge;
import com.example.demo.store.entities.Gender;
import jakarta.validation.constraints.*;
import org.aspectj.lang.annotation.After;

import java.time.LocalDate;

public record UserUpdateRequest(
        @NotBlank
        @Size(max = 100)
        String name,
        @NotNull
        @Size(max = 100)
        String surname,
        @NotBlank
        @Email
        String email,
        @Pattern(regexp = "^\\+?[0-9\\-() ]{7,20}$")
        String phone,
        @NotNull
        Gender gender,
        @Past(message = "Дата рождения не может быть в будущем")
        @MaxAge(value = 100, message = "Дата рождения не может быть более 100 лет назад")
        LocalDate birthday
) {
}
