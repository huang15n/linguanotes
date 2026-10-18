package com.linguanotes.repository;

import com.linguanotes.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long>, JpaSpecificationExecutor<Note> {
    boolean existsByLanguageId(Long languageId);
    long countByFavoriteTrue();
    List<Note> findTop5ByOrderByUpdatedAtDesc();
}
