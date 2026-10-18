package com.linguanotes.repository;

import com.linguanotes.model.ReviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewSessionRepository extends JpaRepository<ReviewSession, Long> {
    List<ReviewSession> findAllByUserIdOrderByStartedAtDesc(Long userId);
}
