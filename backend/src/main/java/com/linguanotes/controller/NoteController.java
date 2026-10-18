package com.linguanotes.controller;

import com.linguanotes.dto.NoteAutosaveRequest;
import com.linguanotes.dto.NoteCommentRequest;
import com.linguanotes.dto.NoteCommentResponse;
import com.linguanotes.dto.NoteLikeResponse;
import com.linguanotes.dto.NoteRequest;
import com.linguanotes.dto.NoteResponse;
import com.linguanotes.model.NoteType;
import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.NoteInteractionService;
import com.linguanotes.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {
    private final NoteService notes;
    private final NoteInteractionService interactions;

    public NoteController(NoteService notes, NoteInteractionService interactions) {
        this.notes = notes;
        this.interactions = interactions;
    }

    @GetMapping
    public List<NoteResponse> search(@AuthenticationPrincipal AccountPrincipal principal,
                                     @RequestParam(required = false) String q,
                                     @RequestParam(required = false) Long languageId,
                                     @RequestParam(required = false) NoteType type,
                                     @RequestParam(required = false) String unit,
                                     @RequestParam(required = false) String tag,
                                     @RequestParam(required = false) Boolean favorite) {
        return notes.search(principal.id(), q, languageId, type, unit, tag, favorite);
    }

    @GetMapping("/{id}")
    public NoteResponse get(@PathVariable Long id, @AuthenticationPrincipal AccountPrincipal principal) {
        return notes.get(id, principal.id());
    }

    @PostMapping
    public ResponseEntity<NoteResponse> create(@Valid @RequestBody NoteRequest request,
                                               @AuthenticationPrincipal AccountPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notes.create(request, principal.id()));
    }

    @PutMapping("/{id}")
    public NoteResponse update(@PathVariable Long id, @Valid @RequestBody NoteRequest request,
                               @AuthenticationPrincipal AccountPrincipal principal) {
        return notes.update(id, request, principal.id());
    }

    @PutMapping("/{id}/autosave")
    public NoteResponse autosave(@PathVariable Long id, @Valid @RequestBody NoteAutosaveRequest request,
                                 @AuthenticationPrincipal AccountPrincipal principal) {
        return notes.autosave(id, request, principal.id());
    }

    @PatchMapping("/{id}/favorite")
    public NoteResponse favorite(@PathVariable Long id, @AuthenticationPrincipal AccountPrincipal principal) {
        return notes.toggleFavorite(id, principal.id());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal AccountPrincipal principal) {
        notes.delete(id, principal.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public NoteLikeResponse like(@PathVariable Long id, @AuthenticationPrincipal AccountPrincipal principal) {
        return interactions.like(id, principal.id());
    }

    @DeleteMapping("/{id}/like")
    public NoteLikeResponse unlike(@PathVariable Long id, @AuthenticationPrincipal AccountPrincipal principal) {
        return interactions.unlike(id, principal.id());
    }

    @GetMapping("/{id}/comments")
    public List<NoteCommentResponse> comments(@PathVariable Long id,
                                               @AuthenticationPrincipal AccountPrincipal principal) {
        return interactions.listComments(id, principal.id());
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<NoteCommentResponse> addComment(@PathVariable Long id,
                                                           @Valid @RequestBody NoteCommentRequest request,
                                                           @AuthenticationPrincipal AccountPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(interactions.addComment(id, principal.id(), request));
    }

    @PutMapping("/{id}/comments/{commentId}")
    public NoteCommentResponse updateComment(@PathVariable Long id, @PathVariable Long commentId,
                                              @Valid @RequestBody NoteCommentRequest request,
                                              @AuthenticationPrincipal AccountPrincipal principal) {
        return interactions.updateComment(id, commentId, principal.id(), request);
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id, @PathVariable Long commentId,
                                               @AuthenticationPrincipal AccountPrincipal principal) {
        interactions.deleteComment(id, commentId, principal.id());
        return ResponseEntity.noContent().build();
    }
}
