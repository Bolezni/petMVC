package com.example.demo.api.service.authentication;

import com.example.demo.api.dto.RegisterRequest;
import com.example.demo.store.entities.Roles;
import com.example.demo.store.entities.UserEntity;
import com.example.demo.store.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BaseRegisterService implements RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        createUser(request, Roles.USER);
    }

    @Override
    public void registerSpecialist(RegisterRequest request) {
        createUser(request, Roles.SPECIALIST);
    }

    private void createUser(RegisterRequest request, Roles role) {
        String email = request.email().trim();
        String phone = request.phone().trim();

        String password = request.password().trim();
        String passwordRepeat = request.passwordRepeat().trim();

        if (!password.equals(passwordRepeat)) {
            log.error("Пароли не совпадают");
            throw new RuntimeException("Пароли не совпадают");
        }

        if (userRepository.existsByEmail(email)) {
            log.error("Пользователь с таким Email уже создан");
            throw new RuntimeException("Пользователь с таким Email уже создан");
        }

        if (userRepository.existsByPhone(phone)) {
            log.error("Пользователь с таким номером телефона уже создан");
            throw new RuntimeException("Пользователь с таким номером телефона уже создан");
        }

        String name = request.name() != null ? request.name().trim() : "anonymous";
        String surname = request.surname() != null ? request.surname().trim() : "";

        UserEntity user = UserEntity.builder()
                .email(email)
                .phone(phone)
                .password(passwordEncoder.encode(password))
                .role(role)
                .name(name)
                .surname(surname)
                .birthday(request.birthday())
                .gender(request.gender())
                .build();

        userRepository.saveAndFlush(user);
    }
}
