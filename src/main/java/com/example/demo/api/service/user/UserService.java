package com.example.demo.api.service.user;

import com.example.demo.api.dto.UserDto;
import com.example.demo.api.dto.UserUpdateRequest;
import com.example.demo.store.entities.UserEntity;

public interface UserService {

    /** Профиль текущего пользователя (получается из SecurityContext). */
    UserDto getMyProfile();

    /** Обновление профиля переданного пользователя; возвращает актуальный DTO. */
    UserDto update(UserEntity currentUser, UserUpdateRequest request);
}
