package com.linguanotes.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "language_levels",
        uniqueConstraints = @UniqueConstraint(name = "uk_language_levels_code", columnNames = {"language_id", "code"}),
        indexes = @Index(name = "idx_language_levels_order", columnList = "language_id, sort_order"))
@Getter
@Setter
@NoArgsConstructor
public class LanguageLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 80)
    private String label;

    @Column(name = "sort_order", nullable = false)
    private int position;
}
