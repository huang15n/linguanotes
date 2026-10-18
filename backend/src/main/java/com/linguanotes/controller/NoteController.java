package com.linguanotes.controller;

import com.linguanotes.dto.Dto.*;
import com.linguanotes.model.NoteType;
import com.linguanotes.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/notes")
public class NoteController {
    private final NoteService service;
    public NoteController(NoteService service) { this.service = service; }

    @GetMapping
    public List<NoteResponse> search(@RequestParam(required = false) String q, @RequestParam(required = false) Long languageId,
                                     @RequestParam(required = false) NoteType type, @RequestParam(required = false) String unit,
                                     @RequestParam(required = false) String tag, @RequestParam(required = false) Boolean favorite) {
        return service.search(q, languageId, type, unit, tag, favorite);
    }
    @GetMapping("/{id}") public NoteResponse get(@PathVariable Long id) { return service.get(id); }
    @PostMapping public ResponseEntity<NoteResponse> create(@Valid @RequestBody NoteRequest r) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r)); }
    @PutMapping("/{id}") public NoteResponse update(@PathVariable Long id, @Valid @RequestBody NoteRequest r) { return service.update(id, r); }
    @PatchMapping("/{id}/favorite") public NoteResponse favorite(@PathVariable Long id) { return service.toggleFavorite(id); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
