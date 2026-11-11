package com.example.demo;

import com.example.demo.api.dto.QuestionnaireCreateDto;
import com.example.demo.api.dto.QuestionnaireResponseDto;
import com.example.demo.api.security.CheckAuthUtil;
import com.example.demo.api.service.questionnaire.QuestionnaireServiceImpl;
import com.example.demo.store.entities.QuestionnaireEntity;
import com.example.demo.store.entities.UserEntity;
import com.example.demo.store.repo.QuestionnaireRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionnaireServiceImplTest {

    @Mock
    private QuestionnaireRepository repository;

    @InjectMocks
    private QuestionnaireServiceImpl service;

    private MockedStatic<CheckAuthUtil> checkAuthUtilMock;

    @BeforeEach
    void setUp() {
        checkAuthUtilMock = Mockito.mockStatic(CheckAuthUtil.class);
    }

    @AfterEach
    void tearDown() {
        checkAuthUtilMock.close(); // обязательно закрыть — иначе мок статик-метода утечёт в другие тесты
    }

    @Test
    void createQuestionnaire_savesEntityWithCurrentUser() {
        UserEntity currentUser = UserEntity.builder().id(1L).email("user@example.com").build();
        checkAuthUtilMock.when(CheckAuthUtil::getAuthUser).thenReturn(currentUser);

        QuestionnaireCreateDto dto = new QuestionnaireCreateDto(
                1,
                "Хочу подкачаться",
                List.of("набрать мышечную массу"),
                "нормальное",
                List.of(),
                null
        );

        service.createQuestionnaire(dto);

        ArgumentCaptor<QuestionnaireEntity> captor = ArgumentCaptor.forClass(QuestionnaireEntity.class);
        verify(repository).save(captor.capture());

        QuestionnaireEntity saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(currentUser);
        assertThat(saved.getSchemaVersion()).isEqualTo(1);
        assertThat(saved.getData()).containsEntry("request", "Хочу подкачаться");
        assertThat(saved.getData()).containsEntry("goals", List.of("набрать мышечную массу"));
    }

    @Test
    void createQuestionnaire_mergesExtraFieldsIntoData() {
        UserEntity currentUser = UserEntity.builder().id(1L).build();
        checkAuthUtilMock.when(CheckAuthUtil::getAuthUser).thenReturn(currentUser);

        QuestionnaireCreateDto dto = new QuestionnaireCreateDto(
                1, "запрос", List.of("цель"), null, null,
                Map.of("experience", "новичок")
        );

        service.createQuestionnaire(dto);

        ArgumentCaptor<QuestionnaireEntity> captor = ArgumentCaptor.forClass(QuestionnaireEntity.class);
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getData()).containsEntry("experience", "новичок");
    }

    @Test
    void getLatestByUserId_returnsDto_whenQuestionnaireExists() {
        UserEntity user = UserEntity.builder().id(5L).build();
        QuestionnaireEntity entity = QuestionnaireEntity.builder()
                .id(10L)
                .user(user)
                .schemaVersion(1)
                .data(Map.of("request", "боль в спине"))
                .build();

        when(repository.findFirstByUserIdOrderByCreatedAtDesc(5L))
                .thenReturn(Optional.of(entity));

        QuestionnaireResponseDto result = service.getQuestionnaireByUserId(5L);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.userId()).isEqualTo(5L);
        assertThat(result.data()).containsEntry("request", "боль в спине");
    }

    @Test
    void getLatestByUserId_throwsException_whenNotFound() {
        when(repository.findFirstByUserIdOrderByCreatedAtDesc(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getQuestionnaireByUserId(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Анкета не найдена");
    }
}