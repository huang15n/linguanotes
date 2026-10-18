package com.linguanotes.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_attempts", indexes = @Index(name = "idx_attempts_user_date", columnList = "user_id, attempted_at"))
@Getter
@Setter
@NoArgsConstructor
public class ExerciseAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal score;

    @Column(name = "answers_json", nullable = false, columnDefinition = "json")
    private String answersJson;

    @Column(name = "attempted_at", nullable = false)
    private LocalDateTime attemptedAt;

    @Column(nullable = false)
    private boolean preview;

    @PrePersist
    void onCreate() {
        attemptedAt = LocalDateTime.now();
    }
}