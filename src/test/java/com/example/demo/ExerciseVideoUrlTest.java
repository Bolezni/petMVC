package com.example.demo;

import com.example.demo.api.dto.ExerciseRequest;
import com.example.demo.api.security.CheckAuthUtil;
import com.example.demo.api.service.exercise.ExerciseServiceImpl;
import com.example.demo.store.entities.Roles;
import com.example.demo.store.entities.UserEntity;
import com.example.demo.store.repo.ExerciseRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Ссылка на видео попадает в href, поэтому в сервис не должно проходить
 * ничего, кроме https. Веб-форма зовёт сервис напрямую, без @Valid.
 */
@ExtendWith(MockitoExtension.class)
class ExerciseVideoUrlTest {

    @Mock
    private ExerciseRepository repository;

    @InjectMocks
    private ExerciseServiceImpl service;

    private MockedStatic<CheckAuthUtil> authMock;

    @BeforeEach
    void setUp() {
        authMock = Mockito.mockStatic(CheckAuthUtil.class);
        authMock.when(CheckAuthUtil::getAuthUser)
                .thenReturn(UserEntity.builder().id(1L).email("spec@example.com").role(Roles.SPECIALIST).build());
    }

    @AfterEach
    void tearDown() {
        authMock.close();
    }

    @Test
    void create_rejectsJavascriptScheme() {
        assertThatThrownBy(() -> service.create(request("javascript:alert(1)")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("https://");

        verify(repository, never()).save(any());
    }

    @Test
    void create_rejectsHttpScheme() {
        assertThatThrownBy(() -> service.create(request("http://example.com/v.mp4")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_rejectsDataScheme() {
        assertThatThrownBy(() -> service.create(request("data:text/html,<script>")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private ExerciseRequest request(String videoUrl) {
        return new ExerciseRequest("Тест", "описание", "Спина", null, videoUrl);
    }
}
