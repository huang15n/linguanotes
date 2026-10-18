package com.linguanotes.repository;

import com.linguanotes.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long>, JpaSpecificationExecutor<Note> {
    boolean existsByLanguageId(Long languageId);
    long countByOwnerId(Long ownerId);
    long countByOwnerIdAndFavoriteTrue(Long ownerId);
    List<Note> findAllByOwnerId(Long ownerId);
    List<Note> findTop5ByOwnerIdOrderByUpdatedAtDesc(Long ownerId);
    Optional<Note> findByIdAndOwnerId(Long id, Long ownerId);
}
