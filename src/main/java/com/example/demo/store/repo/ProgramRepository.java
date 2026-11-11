package com.example.demo.store.repo;

import com.example.demo.store.entities.ProgramEntity;
import com.example.demo.store.entities.ProgramStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramRepository extends JpaRepository<ProgramEntity, Long> {

    @Query("""
            SELECT p FROM ProgramEntity p
            LEFT JOIN FETCH p.items i
            LEFT JOIN FETCH i.exercise
            WHERE p.id = :id
            """)
    Optional<ProgramEntity> findByIdWithItems(@Param("id") Long id);

    List<ProgramEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ProgramEntity> findByUserIdAndStatusNotOrderByCreatedAtDesc(Long userId, ProgramStatus status);
}
