package com.linguanotes.controller;

import com.linguanotes.dto.*;
import com.linguanotes.service.LanguageService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/languages")
public class LanguageController {
    private final LanguageService service;
    public LanguageController(LanguageService service) { this.service = service; }

    @GetMapping public List<LanguageResponse> list() { return service.list(); }
    @PostMapping public ResponseEntity<LanguageResponse> create(@Valid @RequestBody LanguageRequest r) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r)); }
    @PutMapping("/{id}") public LanguageResponse update(@PathVariable Long id, @Valid @RequestBody LanguageRequest r) { return service.update(id, r); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
