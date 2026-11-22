package com.example.demo.api.dto;

import com.example.demo.store.entities.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UserUpdateRequest(
        @NotBlank
        @Size(max = 100)
        String name,
        @NotBlank
        @Size(max = 100)
        String surname,
        @NotBlank
        @Email
        String email,
        @Pattern(regexp = "^\\+?[0-9\\-() ]{7,20}$")
        String phone,
        @NotNull
        Gender gender,
        LocalDate birthday
) {
}
