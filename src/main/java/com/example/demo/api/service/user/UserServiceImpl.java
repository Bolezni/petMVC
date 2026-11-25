package com.example.demo.api.service.user;

import com.example.demo.api.dto.PasswordChangeRequest;
import com.example.demo.api.dto.UserDto;
import com.example.demo.api.dto.UserUpdateRequest;
import com.example.demo.api.exception.exeptions.ConflictException;
import com.example.demo.api.security.CheckAuthUtil;
import com.example.demo.store.entities.UserEntity;
import com.example.demo.store.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

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

        currentUser.setName(safeTrim(request.name()));
        currentUser.setSurname(safeTrim(request.surname()));
        currentUser.setEmail(newEmail);
        currentUser.setPhone(request.phone());
        currentUser.setGender(request.gender());

        if (request.birthday() != null || currentUser.getBirthday() == null) {
            currentUser.setBirthday(request.birthday());
        }

        UserEntity saved = userRepository.saveAndFlush(currentUser);

        log.info("Профиль пользователя id={} обновлён", saved.getId());
        return UserDto.from(saved);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }

    @Override
    @Transactional
    public void passwordChange(PasswordChangeRequest request) throws BadRequestException {
        UserEntity currentUser = CheckAuthUtil.getAuthUser();

        if (!passwordEncoder.matches(request.currentPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Неверный текущий пароль");
        }
        if (passwordEncoder.matches(request.newPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Новый пароль должен отличаться от текущего");
        }

        currentUser.setPassword(passwordEncoder.encode(request.newPassword()));

        userRepository.saveAndFlush(currentUser);
        log.info("Обновлен пароль у пользователя id={}", currentUser.getId());
    }
}
