package com.linguanotes.controller;

import com.linguanotes.dto.VocabularyRequest;
import com.linguanotes.dto.VocabularyResponse;
import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.VocabularyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/vocabulary")
public class VocabularyController {
    private final VocabularyService vocabulary;

    public VocabularyController(VocabularyService vocabulary) {
        this.vocabulary = vocabulary;
    }

    @GetMapping
    public List<VocabularyResponse> list(@AuthenticationPrincipal AccountPrincipal principal) {
        return vocabulary.list(principal.id());
    }

    @PostMapping
    public ResponseEntity<VocabularyResponse> create(@AuthenticationPrincipal AccountPrincipal principal,
                                                     @Valid @RequestBody VocabularyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vocabulary.create(principal.id(), request));
    }

    @PutMapping("/{id}")
    public VocabularyResponse update(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long id,
                                     @Valid @RequestBody VocabularyRequest request) {
        return vocabulary.update(principal.id(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long id) {
        vocabulary.delete(principal.id(), id);
        return ResponseEntity.noContent().build();
    }
}