package com.example.demo;

import com.example.demo.api.exception.exeptions.NotFoundException;
import com.example.demo.api.security.CheckAuthUtil;
import com.example.demo.api.service.media.FileStorage;
import com.example.demo.api.service.media.MediaServiceImpl;
import com.example.demo.store.entities.MediaFileEntity;
import com.example.demo.store.entities.Roles;
import com.example.demo.store.entities.UserEntity;
import com.example.demo.store.repo.ExerciseRepository;
import com.example.demo.store.repo.MediaFileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaServiceImplTest {

    @Mock
    private MediaFileRepository repository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private FileStorage storage;

    @InjectMocks
    private MediaServiceImpl service;

    private MockedStatic<CheckAuthUtil> authMock;
    private UserEntity owner;
    private UserEntity stranger;

    @BeforeEach
    void setUp() {
        authMock = Mockito.mockStatic(CheckAuthUtil.class);

        owner = UserEntity.builder().id(1L).email("owner@example.com").role(Roles.SPECIALIST).build();
        stranger = UserEntity.builder().id(2L).email("stranger@example.com").role(Roles.SPECIALIST).build();
    }

    @AfterEach
    void tearDown() {
        authMock.close();
    }

    @Test
    void delete_removesRowAndFile() {
        MediaFileEntity media = media(owner, "1/abc.mp4");
        when(repository.findById(5L)).thenReturn(Optional.of(media));
        authMock.when(CheckAuthUtil::getAuthUser).thenReturn(owner);

        service.delete(5L);

        verify(repository).delete(media);
        verify(storage).delete("1/abc.mp4");
    }

    @Test
    void delete_foreignMedia_isNotFoundAndFileUntouched() {
        MediaFileEntity media = media(owner, "1/abc.mp4");
        when(repository.findById(5L)).thenReturn(Optional.of(media));
        authMock.when(CheckAuthUtil::getAuthUser).thenReturn(stranger);

        assertThatThrownBy(() -> service.delete(5L))
                .isInstanceOf(NotFoundException.class);

        verify(repository, never()).delete(media);
        verify(storage, never()).delete("1/abc.mp4");
    }

    @Test
    void delete_missingMedia_isNotFound() {
        when(repository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(7L))
                .isInstanceOf(NotFoundException.class);
    }

    private MediaFileEntity media(UserEntity user, String key) {
        MediaFileEntity entity = new MediaFileEntity();
        entity.setId(5L);
        entity.setUser(user);
        entity.setStorageKey(key);
        return entity;
    }
}
