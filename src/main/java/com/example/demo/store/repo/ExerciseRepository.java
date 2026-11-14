package com.example.demo.store.repo;

import com.example.demo.store.entities.ExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRepository
        extends JpaRepository<ExerciseEntity, Long>, JpaSpecificationExecutor<ExerciseEntity> {

    Optional<ExerciseEntity> findByIdAndActiveTrue(Long id);

    List<ExerciseEntity> findAllByIdInAndActiveTrue(Collection<Long> ids);

    /** Видео-демонстрация, привязанная к активному упражнению, доступна всем пользователям. */
    boolean existsByVideoMediaIdAndActiveTrue(Long videoMediaId);
}
