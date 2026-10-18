package com.linguanotes.repository;

import com.linguanotes.model.VocabularyEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VocabularyEntryRepository extends JpaRepository<VocabularyEntry, Long> {
    long countByOwnerId(Long ownerId);
    List<VocabularyEntry> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    Optional<VocabularyEntry> findByIdAndOwnerId(Long id, Long ownerId);
}