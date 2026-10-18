package com.linguanotes.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity @Table(name = "notes") @Getter @Setter @NoArgsConstructor
public class Note {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "owner_id", nullable = false) private UserAccount owner;
    @Column(nullable = false) private String title;
    @Column(columnDefinition = "TEXT") private String content;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "lesson_id") private Lesson lesson;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "language_id") private Language language;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private NoteType type = NoteType.OTHER;
    private String unit;
    private boolean favorite;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "note_tags", joinColumns = @JoinColumn(name = "note_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }
}
