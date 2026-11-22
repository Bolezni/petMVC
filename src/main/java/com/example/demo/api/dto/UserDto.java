package com.example.demo.api.dto;

import com.example.demo.store.entities.Gender;
import com.example.demo.store.entities.UserEntity;

public record UserDto(
        Long id,
        String email,
        String name,
        String surname,
        String phone,
        Gender gender,
        Integer age

) {
    public static UserDto from(UserEntity entity){
        return new UserDto(entity.getId(), entity.getEmail(), entity.getName(), entity.getSurname(),
                entity.getPhone(), entity.getGender(), entity.getAge());
    }
}
