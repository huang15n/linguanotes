package com.linguanotes.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Contenu de préparation à un examen officiel (IELTS, TCF, TEF, DELF, DELE).
 * Entité séparée de {@link Note} : mêmes idées (titre, contenu Markdown, favori,
 * version / autosave) mais métadonnées spécifiques aux examens.
 * Les fichiers audio (listening / speaking) pourront être liés plus tard
 * via {@link StoredFile} ou une relation dédiée.
 */
@Entity
@Table(name = "test_preparations",
        indexes = {
                @Index(name = "idx_test_prep_owner", columnList = "owner_id"),
                @Index(name = "idx_test_prep_exam", columnList = "exam_language, exam_type, skill_module")
        })
@Getter
@Setter
@NoArgsConstructor
public class TestPreparation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserAccount owner;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    /** Langue de l'examen (EN / FR / ES), indépendante de languages. */
    @Enumerated(EnumType.STRING)
    @Column(name = "exam_language", nullable = false, length = 20)
    private ExamLanguage examLanguage;

    /** Type d'examen : IELTS, TCF, TEF, DELF, DELE. */
    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false, length = 20)
    private ExamType examType;

    /**
     * Niveau visé : IELTS 6–9, CECRL A1–C2 pour TCF/TEF/DELF/DELE.
     * Stocké en chaîne pour rester flexible (ex. "B2", "IELTS_7").
     */
    @Column(name = "exam_level", nullable = false, length = 30)
    private String examLevel;

    /** Module de compétence (listening / speaking / reading / writing). */
    @Enumerated(EnumType.STRING)
    @Column(name = "skill_module", nullable = false, length = 20)
    private ExamSkillModule skillModule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoteVisibility visibility = NoteVisibility.PRIVATE;

    @Version
    @Column(nullable = false)
    private int version;

    private boolean favorite;

    private LocalDateTime lastSavedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Référence optionnelle à un fichier audio (compréhension / production orale).
     * Rempli plus tard quand l'upload audio sera branché.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_file_id")
    private StoredFile audioFile;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = lastSavedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = lastSavedAt = LocalDateTime.now();
    }
}
