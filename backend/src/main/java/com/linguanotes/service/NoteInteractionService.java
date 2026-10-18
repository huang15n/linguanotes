package com.linguanotes.service;

import com.linguanotes.dto.NoteCommentRequest;
import com.linguanotes.dto.NoteCommentResponse;
import com.linguanotes.dto.NoteLikeResponse;
import com.linguanotes.model.Note;
import com.linguanotes.model.NoteComment;
import com.linguanotes.model.NoteLike;
import com.linguanotes.repository.NoteCommentRepository;
import com.linguanotes.repository.NoteLikeRepository;
import com.linguanotes.repository.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class NoteInteractionService {
    private final NoteService notes;
    private final NoteLikeRepository likes;
    private final NoteCommentRepository comments;
    private final UserAccountRepository accounts;

    public NoteInteractionService(NoteService notes, NoteLikeRepository likes,
                                  NoteCommentRepository comments, UserAccountRepository accounts) {
        this.notes = notes;
        this.likes = likes;
        this.comments = comments;
        this.accounts = accounts;
    }

    public NoteLikeResponse like(Long noteId, Long userId) {
        Note note = notes.requireReadable(noteId, userId);
        if (!likes.existsByNoteIdAndUserId(noteId, userId)) {
            NoteLike like = new NoteLike();
            like.setNote(note);
            like.setUser(accounts.getReferenceById(userId));
            likes.save(like);
        }
        return likeResponse(noteId, userId);
    }

    public NoteLikeResponse unlike(Long noteId, Long userId) {
        notes.requireReadable(noteId, userId);
        likes.deleteByNoteIdAndUserId(noteId, userId);
        return likeResponse(noteId, userId);
    }

    @Transactional(readOnly = true)
    public List<NoteCommentResponse> listComments(Long noteId, Long userId) {
        notes.requireReadable(noteId, userId);
        return comments.findAllByNoteIdOrderByCreatedAtAsc(noteId).stream()
                .map(comment -> map(comment, userId))
                .toList();
    }

    public NoteCommentResponse addComment(Long noteId, Long userId, NoteCommentRequest request) {
        Note note = notes.requireReadable(noteId, userId);
        NoteComment comment = new NoteComment();
        comment.setNote(note);
        comment.setAuthor(accounts.getReferenceById(userId));
        comment.setContent(request.content().trim());
        return map(comments.saveAndFlush(comment), userId);
    }

    public NoteCommentResponse updateComment(Long noteId, Long commentId, Long userId,
                                             NoteCommentRequest request) {
        NoteComment comment = ownedComment(noteId, commentId, userId);
        comment.setContent(request.content().trim());
        return map(comments.saveAndFlush(comment), userId);
    }

    public void deleteComment(Long noteId, Long commentId, Long userId) {
        comments.delete(ownedComment(noteId, commentId, userId));
    }

    private NoteComment ownedComment(Long noteId, Long commentId, Long userId) {
        notes.requireReadable(noteId, userId);
        return comments.findByIdAndAuthorId(commentId, userId)
                .filter(comment -> comment.getNote().getId().equals(noteId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Commentaire introuvable"));
    }

    private NoteLikeResponse likeResponse(Long noteId, Long userId) {
        return new NoteLikeResponse(noteId, likes.countByNoteId(noteId),
                likes.existsByNoteIdAndUserId(noteId, userId));
    }

    private NoteCommentResponse map(NoteComment comment, Long userId) {
        return new NoteCommentResponse(comment.getId(), comment.getNote().getId(),
                comment.getContent(), comment.getAuthor().getId(), comment.getAuthor().getName(),
                comment.getAuthor().getId().equals(userId), comment.getCreatedAt(),
                comment.getUpdatedAt());
    }
}
