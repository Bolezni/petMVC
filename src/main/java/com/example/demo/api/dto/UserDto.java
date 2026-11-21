package com.example.demo.api.dto;

import com.example.demo.store.entities.Gender;
import com.example.demo.store.entities.Roles;
import com.example.demo.store.entities.UserEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Выходной DTO профиля. Пароль и другие чувствительные поля не отдаём наружу.
 */
public record UserDto(
        Long id,
        Roles role,
        String email,
        String phone,
        String name,
        String surname,
        Gender gender,
        Integer age,
        LocalDate birthday,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserDto from(UserEntity u) {
        return new UserDto(
                u.getId(),
                u.getRole(),
                u.getEmail(),
                u.getPhone(),
                u.getName(),
                u.getSurname(),
                u.getGender(),
                u.getAge(),
                u.getBirthday(),
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }
}
