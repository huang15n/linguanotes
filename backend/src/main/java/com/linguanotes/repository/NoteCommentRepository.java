package com.linguanotes.repository;

import com.linguanotes.model.NoteComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NoteCommentRepository extends JpaRepository<NoteComment, Long> {
    List<NoteComment> findAllByNoteIdOrderByCreatedAtAsc(Long noteId);
    long countByNoteId(Long noteId);
    Optional<NoteComment> findByIdAndAuthorId(Long id, Long authorId);
}
