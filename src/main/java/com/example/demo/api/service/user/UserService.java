package com.example.demo.api.service.user;

import com.example.demo.api.dto.PasswordChangeRequest;
import com.example.demo.api.dto.UserDto;
import com.example.demo.api.dto.UserUpdateRequest;
import com.example.demo.store.entities.UserEntity;
import org.apache.coyote.BadRequestException;

public interface UserService {
    UserDto getMyProfile();

    UserDto update(UserEntity currentUser, UserUpdateRequest request);

    void passwordChange(PasswordChangeRequest request) throws BadRequestException;
}
