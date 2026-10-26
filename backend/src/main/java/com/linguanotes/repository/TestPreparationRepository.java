package com.linguanotes.repository;

import com.linguanotes.model.ExamLanguage;
import com.linguanotes.model.ExamSkillModule;
import com.linguanotes.model.ExamType;
import com.linguanotes.model.PublicationStatus;
import com.linguanotes.model.TestPreparation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TestPreparationRepository extends JpaRepository<TestPreparation, Long> {

    Optional<TestPreparation> findByIdAndOwnerId(Long id, Long ownerId);

    long countByOwnerId(Long ownerId);

    long countByOwnerIdAndFavoriteTrue(Long ownerId);

    List<TestPreparation> findTop5ByOwnerIdOrderByUpdatedAtDesc(Long ownerId);

    @Query("""
            SELECT t FROM TestPreparation t
            WHERE (
                    t.publicationStatus = com.linguanotes.model.PublicationStatus.PUBLISHED
                    OR t.owner.id = :viewerId
                    OR :isAdmin = true
                  )
              AND (:q IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(COALESCE(t.content, '')) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:examLanguage IS NULL OR t.examLanguage = :examLanguage)
              AND (:examType IS NULL OR t.examType = :examType)
              AND (:examLevel IS NULL OR t.examLevel = :examLevel)
              AND (:skillModule IS NULL OR t.skillModule = :skillModule)
              AND (:favorite IS NULL OR t.favorite = :favorite)
              AND (:status IS NULL OR t.publicationStatus = :status)
              AND (:mineOnly = false OR t.owner.id = :viewerId)
            ORDER BY t.updatedAt DESC
            """)
    List<TestPreparation> searchVisible(
            @Param("viewerId") Long viewerId,
            @Param("isAdmin") boolean isAdmin,
            @Param("q") String q,
            @Param("examLanguage") ExamLanguage examLanguage,
            @Param("examType") ExamType examType,
            @Param("examLevel") String examLevel,
            @Param("skillModule") ExamSkillModule skillModule,
            @Param("favorite") Boolean favorite,
            @Param("status") PublicationStatus status,
            @Param("mineOnly") boolean mineOnly
    );
}
