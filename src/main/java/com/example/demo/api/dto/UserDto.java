package com.example.demo.api.dto;

import com.example.demo.store.entities.Gender;
import com.example.demo.store.entities.Roles;
import com.example.demo.store.entities.UserEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserDto(
        Long id,
        String name,
        String surname,
        String email,
        String phone,
        Gender gender,
        Integer age,
        LocalDate birthday,
        LocalDateTime createdAt,
        Roles role,
        Long avatarId

) {
    public static UserDto from(UserEntity e) {
        return new UserDto(
                e.getId(),
                e.getName(),
                e.getSurname(),
                e.getEmail(),
                e.getPhone(),
                e.getGender(),
                e.getAge(),
                e.getBirthday(),
                e.getCreatedAt(),
                e.getRole(),
                e.getAvatar() != null ? e.getAvatar().getId() : null
        );
    }
}
