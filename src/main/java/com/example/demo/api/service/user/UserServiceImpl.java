package com.example.demo.api.service.user;

import com.example.demo.api.dto.UserDto;
import com.example.demo.api.dto.UserUpdateRequest;
import com.example.demo.api.exception.exeptions.ConflictException;
import com.example.demo.api.security.CheckAuthUtil;
import com.example.demo.store.entities.UserEntity;
import com.example.demo.store.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDto getMyProfile() {
        return UserDto.from(CheckAuthUtil.getAuthUser());
    }

    @Override
    @Transactional
    public UserDto update(UserEntity currentUser, UserUpdateRequest request) {
        String newEmail = normalizeEmail(request.email());

        if (!newEmail.equalsIgnoreCase(currentUser.getEmail())
                && userRepository.findByEmail(newEmail).isPresent()) {
            throw new ConflictException("Email уже занят");
        }

        currentUser.setName(request.name().trim());
        currentUser.setSurname(request.surname().trim());
        currentUser.setEmail(newEmail);
        currentUser.setPhone(request.phone());
        currentUser.setGender(request.gender());
        currentUser.setBirthday(request.birthday());

        UserEntity saved = userRepository.saveAndFlush(currentUser);

        log.info("Профиль пользователя id={} обновлён", saved.getId());
        return UserDto.from(saved);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
