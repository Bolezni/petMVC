package com.example.demo.api.service.user;

import com.example.demo.api.dto.UserDto;
import com.example.demo.api.dto.UserUpdateRequest;
import com.example.demo.store.entities.UserEntity;

public interface UserService {
    UserDto getMyProfile();

    UserDto update(UserEntity currentUser, UserUpdateRequest request);
}
