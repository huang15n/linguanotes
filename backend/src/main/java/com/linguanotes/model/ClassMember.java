package com.linguanotes.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "class_members",
        uniqueConstraints = @UniqueConstraint(name = "uk_class_members", columnNames = {"class_id", "user_id"}),
        indexes = @Index(name = "idx_class_members_user", columnList = "user_id"))
@Getter
@Setter
@NoArgsConstructor
public class ClassMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_role", nullable = false, length = 20)
    private ClassMemberRole role = ClassMemberRole.STUDENT;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    void onCreate() {
        joinedAt = LocalDateTime.now();
    }
}
