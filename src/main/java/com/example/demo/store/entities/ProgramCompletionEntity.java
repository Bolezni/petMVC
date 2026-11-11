package com.example.demo.store.entities;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "program_completion", indexes = {
        @Index(name = "idx_completion_user", columnList = "user_id"),
        @Index(name = "idx_completion_program_item", columnList = "program_item_id")
})
@Entity
public class ProgramCompletionEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_item_id", nullable = false)
    private ProgramItemEntity programItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
}
