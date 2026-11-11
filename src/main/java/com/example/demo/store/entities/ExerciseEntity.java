package com.example.demo.store.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Библиотека упражнений
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "exercise", indexes = {
        @Index(name = "idx_exercise_muscle_group", columnList = "muscle_group")
})
public class ExerciseEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "muscle_group", nullable = false)
    private String muscleGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private Difficulty difficulty;

    @Column(name = "video_url", length = 512)
    private String videoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
