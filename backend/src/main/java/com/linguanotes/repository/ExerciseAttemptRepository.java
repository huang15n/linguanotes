package com.linguanotes.repository;

import com.linguanotes.model.ExerciseAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ExerciseAttemptRepository extends JpaRepository<ExerciseAttempt, Long> {
    long countByUserId(Long userId);
    List<ExerciseAttempt> findAllByUserIdOrderByAttemptedAtDesc(Long userId);
    List<ExerciseAttempt> findAllByUserIdAndAttemptedAtAfterOrderByAttemptedAtAsc(Long userId, LocalDateTime since);

    @Query("select avg(a.score) from ExerciseAttempt a where a.user.id = :userId")
    Double averageScoreByUserId(@Param("userId") Long userId);
}