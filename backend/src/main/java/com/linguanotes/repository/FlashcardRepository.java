package com.linguanotes.repository;

import com.linguanotes.model.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    List<Flashcard> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    Optional<Flashcard> findByIdAndOwnerId(Long id, Long ownerId);
}