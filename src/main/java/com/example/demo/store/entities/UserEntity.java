package com.example.demo.store.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Period;


/**
 * Сущность пользователь
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "user_entity")
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Roles role;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "telegram_id", unique = true)
    private Long telegramId;

    private String name;

    private String surname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    private LocalDate birthday;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_id")
    private MediaFileEntity avatar;


    public Integer getAge() {
        if (birthday != null)
            return Period.between(birthday, LocalDate.now()).getYears();
        return null;
    }
}