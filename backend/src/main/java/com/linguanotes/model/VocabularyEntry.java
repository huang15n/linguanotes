package com.linguanotes.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vocabulary_entries", indexes = @Index(name = "idx_vocabulary_owner_source", columnList = "owner_id, source_text"))
@Getter
@Setter
@NoArgsConstructor
public class VocabularyEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserAccount owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_language_id", nullable = false)
    private Language sourceLanguage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_language_id", nullable = false)
    private Language targetLanguage;

    @Column(nullable = false, length = 500)
    private String sourceText;

    @Column(nullable = false, length = 1000)
    private String translation;

    @Column(length = 255)
    private String ipa;

    @Column(length = 80)
    private String partOfSpeech;

    @Column(columnDefinition = "json")
    private String examplesJson;

    @Column(columnDefinition = "TEXT")
    private String personalNotes;

    @Column(nullable = false)
    private int difficulty = 1;

    @Column(name = "audio_file_id")
    private Long audioFileId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "vocabulary_entry_tags",
            joinColumns = @JoinColumn(name = "vocabulary_entry_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}