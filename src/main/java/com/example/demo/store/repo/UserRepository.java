package com.example.demo.store.repo;

import com.example.demo.store.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findById(Long id);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    @Query("""
        SELECT DISTINCT u FROM UserEntity u
        WHERE EXISTS (SELECT 1 FROM QuestionnaireEntity q WHERE q.user = u)
        AND u.role = 'USER'
        """)
    List<UserEntity> findAllWithQuestionnaire();

}
