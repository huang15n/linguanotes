package com.linguanotes.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "exercises", indexes = @Index(name = "idx_exercises_owner", columnList = "owner_id"))
@Getter
@Setter
@NoArgsConstructor
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserAccount owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id")
    private Language language;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(length = 30)
    private String level;

    @Column(name = "grammar_topic", length = 180)
    private String grammarTopic;

    @Column(columnDefinition = "LONGTEXT")
    private String introduction;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String instructions;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", nullable = false, length = 32)
    private ExerciseType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExerciseStatus status = ExerciseStatus.PUBLISHED;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    @Column(name = "pass_threshold", nullable = false)
    private int passThreshold = 60;

    @Column(name = "show_answers", nullable = false)
    private boolean showAnswers = true;

    @Column(name = "questions_json", nullable = false, columnDefinition = "json")
    private String questionsJson;

    @Column(name = "explanations_json", columnDefinition = "json")
    private String explanationsJson;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}