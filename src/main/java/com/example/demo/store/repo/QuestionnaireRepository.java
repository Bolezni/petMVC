package com.example.demo.store.repo;

import com.example.demo.api.dto.UserListItemDto;
import com.example.demo.store.entities.QuestionnaireEntity;
import com.example.demo.store.entities.Roles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionnaireRepository extends JpaRepository<QuestionnaireEntity, Long> {

    Optional<QuestionnaireEntity> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    @Query(value = """
            SELECT new com.example.demo.api.dto.UserListItemDto(
                u.id, u.email, u.phone, MAX(q.createdAt))
            FROM QuestionnaireEntity q
            JOIN q.user u
            WHERE u.role = :role
            GROUP BY u.id, u.email, u.phone
            ORDER BY MAX(q.createdAt) DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT u.id)
            FROM QuestionnaireEntity q
            JOIN q.user u
            WHERE u.role = :role
            """)
    Page<UserListItemDto> findUsersWithQuestionnaire(@Param("role") Roles role, Pageable pageable);

}
