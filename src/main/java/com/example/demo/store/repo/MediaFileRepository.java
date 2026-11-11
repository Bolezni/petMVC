package com.example.demo.store.repo;

import com.example.demo.store.entities.MediaFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFileEntity, Long> {
    List<MediaFileEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
