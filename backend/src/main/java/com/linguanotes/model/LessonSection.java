package com.linguanotes.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_sections",
        indexes = @Index(name = "idx_lesson_sections_lesson_order", columnList = "lesson_id, sort_order"))
@Getter
@Setter
@NoArgsConstructor
public class LessonSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", nullable = false, length = 32)
    private LessonSectionType type;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "content_json", columnDefinition = "json")
    private String contentJson;

    @Column(name = "sort_order", nullable = false)
    private int position;

    @Column(nullable = false)
    private boolean visible = true;

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
