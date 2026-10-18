package com.linguanotes.repository;

import com.linguanotes.model.AiGeneration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface AiGenerationRepository extends JpaRepository<AiGeneration, Long> {
    long countByOwnerIdAndCreatedAtAfter(Long ownerId, LocalDateTime after);
}