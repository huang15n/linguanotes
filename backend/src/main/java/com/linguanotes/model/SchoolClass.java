package com.linguanotes.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "classes",
        uniqueConstraints = @UniqueConstraint(name = "uk_classes_join_code", columnNames = "join_code"),
        indexes = @Index(name = "idx_classes_teacher", columnList = "teacher_id"))
@Getter
@Setter
@NoArgsConstructor
public class SchoolClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private UserAccount teacher;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "join_code", nullable = false, length = 12)
    private String joinCode;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
