package com.example.demo.store.repo;

import com.example.demo.store.entities.ProgramItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProgramItemRepository extends JpaRepository<ProgramItemEntity, Long> {

    @Query("""
            SELECT i FROM ProgramItemEntity i
            JOIN FETCH i.program p
            JOIN FETCH p.user
            JOIN FETCH i.exercise
            WHERE i.id = :id
            """)
    Optional<ProgramItemEntity> findByIdWithProgram(@Param("id") Long id);
}
