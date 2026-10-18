package com.linguanotes.controller;

import com.linguanotes.dto.LanguageLevelResponse;
import com.linguanotes.dto.LanguageRequest;
import com.linguanotes.dto.LanguageResponse;
import com.linguanotes.service.LanguageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/languages")
public class LanguageController {
    private final LanguageService service;

    public LanguageController(LanguageService service) {
        this.service = service;
    }

    @GetMapping
    public List<LanguageResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}/levels")
    public List<LanguageLevelResponse> levels(@PathVariable Long id) {
        return service.listLevels(id);
    }

    @PostMapping
    public ResponseEntity<LanguageResponse> create(@Valid @RequestBody LanguageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public LanguageResponse update(@PathVariable Long id, @Valid @RequestBody LanguageRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
