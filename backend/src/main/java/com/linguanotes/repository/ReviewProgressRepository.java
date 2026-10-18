package com.linguanotes.repository;

import com.linguanotes.model.ReviewProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewProgressRepository extends JpaRepository<ReviewProgress, Long> {
    long countByUserIdAndNextDueAtLessThanEqual(Long userId, LocalDateTime now);
    List<ReviewProgress> findAllByUserIdAndNextDueAtLessThanEqualOrderByNextDueAtAsc(Long userId, LocalDateTime now);
    Optional<ReviewProgress> findByUserIdAndFlashcardId(Long userId, Long flashcardId);
}