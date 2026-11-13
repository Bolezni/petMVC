package com.example.demo.store.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Программа, назначенная пользователю
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "program", indexes = {
        @Index(name = "idx_program_user", columnList = "user_id"),
        @Index(name = "idx_program_specialist", columnList = "specialist_id")
})
public class ProgramEntity extends BaseEntity {

    /** Значения по умолчанию, если специалист не указал нагрузку. */
    public static final int DEFAULT_SETS = 3;
    public static final int DEFAULT_REPS = 12;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialist_id", nullable = false)
    private UserEntity specialist;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "specialist_comment", columnDefinition = "TEXT")
    private String specialistComment;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "priorities", columnDefinition = "jsonb")
    private List<String> priorities;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProgramStatus status = ProgramStatus.ACTIVE;

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ProgramItemEntity> items = new ArrayList<>();

    public void addItem(ExerciseEntity exercise, Integer sets, Integer reps, String note, int order) {
        ProgramItemEntity item = new ProgramItemEntity();
        item.setProgram(this);
        item.setExercise(exercise);
        // незаполненная нагрузка не должна превращаться в null на странице
        item.setSets(sets == null ? DEFAULT_SETS : sets);
        item.setReps(reps == null ? DEFAULT_REPS : reps);
        item.setSpecialistNote(note);
        item.setSortOrder(order);
        items.add(item);
    }
}
