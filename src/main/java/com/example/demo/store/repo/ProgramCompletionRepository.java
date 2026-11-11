package com.example.demo.store.repo;

import com.example.demo.store.entities.ProgramCompletionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProgramCompletionRepository extends JpaRepository<ProgramCompletionEntity, Long> {

    @Query(value = """
            SELECT c FROM ProgramCompletionEntity c
            JOIN FETCH c.programItem i
            JOIN FETCH i.exercise
            WHERE i.program.id = :programId
            ORDER BY c.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(c) FROM ProgramCompletionEntity c
            WHERE c.programItem.program.id = :programId
            """)
    Page<ProgramCompletionEntity> findByProgramId(@Param("programId") Long programId, Pageable pageable);
}
