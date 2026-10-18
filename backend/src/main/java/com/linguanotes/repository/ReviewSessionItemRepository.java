package com.linguanotes.repository;

import com.linguanotes.model.ReviewSessionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewSessionItemRepository extends JpaRepository<ReviewSessionItem, Long> {
    List<ReviewSessionItem> findAllBySessionId(Long sessionId);
}
