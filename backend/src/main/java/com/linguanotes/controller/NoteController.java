package com.linguanotes.controller;

import com.linguanotes.dto.*;
import com.linguanotes.model.NoteType;
import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/notes")
public class NoteController {
    private final NoteService service;
    public NoteController(NoteService service) { this.service = service; }

    @GetMapping
    public List<NoteResponse> search(@AuthenticationPrincipal AccountPrincipal principal,
                                     @RequestParam(required = false) String q, @RequestParam(required = false) Long languageId,
                                     @RequestParam(required = false) NoteType type, @RequestParam(required = false) String unit,
                                     @RequestParam(required = false) String tag, @RequestParam(required = false) Boolean favorite) {
        return service.search(principal.id(), q, languageId, type, unit, tag, favorite);
    }
    @GetMapping("/{id}") public NoteResponse get(@PathVariable Long id, @AuthenticationPrincipal AccountPrincipal principal) { return service.get(id, principal.id()); }
    @PostMapping public ResponseEntity<NoteResponse> create(@Valid @RequestBody NoteRequest r, @AuthenticationPrincipal AccountPrincipal principal) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r, principal.id())); }
    @PutMapping("/{id}") public NoteResponse update(@PathVariable Long id, @Valid @RequestBody NoteRequest r, @AuthenticationPrincipal AccountPrincipal principal) { return service.update(id, r, principal.id()); }
    @PatchMapping("/{id}/favorite") public NoteResponse favorite(@PathVariable Long id, @AuthenticationPrincipal AccountPrincipal principal) { return service.toggleFavorite(id, principal.id()); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal AccountPrincipal principal) { service.delete(id, principal.id()); return ResponseEntity.noContent().build(); }
}
