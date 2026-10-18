package com.linguanotes.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_languages",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_languages", columnNames = {"user_id", "language_id"}),
        indexes = @Index(name = "idx_user_languages_user", columnList = "user_id"))
@Getter
@Setter
@NoArgsConstructor
public class UserLanguage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(length = 30)
    private String level;

    @Column(nullable = false)
    private boolean target = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
