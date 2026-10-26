package com.linguanotes.repository;

import com.linguanotes.model.ExamLanguage;
import com.linguanotes.model.ExamSkillModule;
import com.linguanotes.model.ExamType;
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
            WHERE t.owner.id = :ownerId
              AND (:q IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(COALESCE(t.content, '')) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:examLanguage IS NULL OR t.examLanguage = :examLanguage)
              AND (:examType IS NULL OR t.examType = :examType)
              AND (:examLevel IS NULL OR t.examLevel = :examLevel)
              AND (:skillModule IS NULL OR t.skillModule = :skillModule)
              AND (:favorite IS NULL OR t.favorite = :favorite)
            ORDER BY t.updatedAt DESC
            """)
    List<TestPreparation> search(
            @Param("ownerId") Long ownerId,
            @Param("q") String q,
            @Param("examLanguage") ExamLanguage examLanguage,
            @Param("examType") ExamType examType,
            @Param("examLevel") String examLevel,
            @Param("skillModule") ExamSkillModule skillModule,
            @Param("favorite") Boolean favorite
    );
}
