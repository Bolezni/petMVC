package com.example.demo;

import com.example.demo.api.dto.ProgramCreateRequest;
import com.example.demo.api.dto.ProgramItemRequest;
import com.example.demo.api.dto.ProgramResponse;
import com.example.demo.api.dto.ProgramUpdateRequest;
import com.example.demo.api.security.CheckAuthUtil;
import com.example.demo.api.service.program.ProgramServiceImpl;
import com.example.demo.store.entities.ExerciseEntity;
import com.example.demo.store.entities.ProgramEntity;
import com.example.demo.store.entities.Roles;
import com.example.demo.store.entities.UserEntity;
import com.example.demo.store.repo.ExerciseRepository;
import com.example.demo.store.repo.ProgramRepository;
import com.example.demo.store.repo.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgramServiceImplTest {

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private ProgramServiceImpl service;

    private MockedStatic<CheckAuthUtil> authMock;
    private UserEntity specialist;
    private UserEntity user;
    private ExerciseEntity exercise;

    @BeforeEach
    void setUp() {
        authMock = Mockito.mockStatic(CheckAuthUtil.class);

        specialist = UserEntity.builder().id(1L).email("spec@example.com").role(Roles.SPECIALIST).build();
        user = UserEntity.builder().id(2L).email("user@example.com").role(Roles.USER).build();

        exercise = new ExerciseEntity();
        exercise.setId(10L);
        exercise.setName("Мостик");
        exercise.setMuscleGroup("Ягодицы");

        authMock.when(CheckAuthUtil::getAuthUser).thenReturn(specialist);
    }

    @AfterEach
    void tearDown() {
        authMock.close();
    }

    @Test
    void create_fillsMissingSetsAndRepsWithDefaults() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(exerciseRepository.findAllByIdInAndActiveTrue(List.of(10L))).thenReturn(List.of(exercise));
        when(programRepository.save(any(ProgramEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ProgramCreateRequest request = new ProgramCreateRequest(
                2L, "Программа", null, List.of(),
                List.of(new ProgramItemRequest(10L, null, 7, null)));

        service.create(request);

        ArgumentCaptor<ProgramEntity> captor = ArgumentCaptor.forClass(ProgramEntity.class);
        Mockito.verify(programRepository).save(captor.capture());

        ProgramEntity saved = captor.getValue();
        assertThat(saved.getItems()).hasSize(1);
        assertThat(saved.getItems().get(0).getSets()).isEqualTo(ProgramEntity.DEFAULT_SETS);
        assertThat(saved.getItems().get(0).getReps()).isEqualTo(7);
    }

    @Test
    void create_keepsExplicitValues() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(exerciseRepository.findAllByIdInAndActiveTrue(List.of(10L))).thenReturn(List.of(exercise));
        when(programRepository.save(any(ProgramEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ProgramCreateRequest request = new ProgramCreateRequest(
                2L, "Программа", null, List.of(),
                List.of(new ProgramItemRequest(10L, 4, 15, "Медленно")));

        service.create(request);

        ArgumentCaptor<ProgramEntity> captor = ArgumentCaptor.forClass(ProgramEntity.class);
        Mockito.verify(programRepository).save(captor.capture());

        assertThat(captor.getValue().getItems().get(0).getSets()).isEqualTo(4);
        assertThat(captor.getValue().getItems().get(0).getReps()).isEqualTo(15);
    }

    @Test
    void update_replacesItemsAndAppliesDefaults() {
        ExerciseEntity second = new ExerciseEntity();
        second.setId(11L);
        second.setName("Кошка");
        second.setMuscleGroup("Спина");

        ProgramEntity program = new ProgramEntity();
        program.setUser(user);
        program.setSpecialist(specialist);
        program.setTitle("Старое название");
        program.addItem(exercise, 5, 5, null, 0);

        when(programRepository.findByIdWithItems(3L)).thenReturn(Optional.of(program));
        when(exerciseRepository.findAllByIdInAndActiveTrue(List.of(11L))).thenReturn(List.of(second));
        when(programRepository.save(any(ProgramEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ProgramResponse response = service.update(3L, new ProgramUpdateRequest(
                "Новое название", "Комментарий", List.of("Регулярность"),
                List.of(new ProgramItemRequest(11L, null, null, "Плавно"))));

        assertThat(response.title()).isEqualTo("Новое название");
        assertThat(program.getItems()).hasSize(1);
        assertThat(program.getItems().get(0).getExercise().getId()).isEqualTo(11L);
        assertThat(program.getItems().get(0).getSets()).isEqualTo(ProgramEntity.DEFAULT_SETS);
        assertThat(program.getItems().get(0).getReps()).isEqualTo(ProgramEntity.DEFAULT_REPS);
        assertThat(program.getItems().get(0).getSortOrder()).isZero();
        assertThat(program.getPriorities()).containsExactly("Регулярность");
    }
}
