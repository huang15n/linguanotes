package com.linguanotes.service;

import com.linguanotes.dto.Dto.*;
import com.linguanotes.model.Language;
import com.linguanotes.repository.LanguageRepository;
import com.linguanotes.repository.NoteRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class LanguageService {
    private final LanguageRepository repo; private final NoteRepository notes;
    public LanguageService(LanguageRepository repo, NoteRepository notes) { this.repo = repo; this.notes = notes; }

    public List<LanguageResponse> list() { return repo.findAll(Sort.by("name")).stream().map(this::map).toList(); }
    public LanguageResponse create(LanguageRequest r) { return map(repo.save(new Language(null, r.name().trim(), r.code().trim()))); }
    public LanguageResponse update(Long id, LanguageRequest r) {
        Language l = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Langue introuvable"));
        l.setName(r.name().trim()); l.setCode(r.code().trim()); return map(repo.save(l));
    }
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Langue introuvable");
        if (notes.existsByLanguageId(id)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette langue est utilisée par des notes");
        repo.deleteById(id);
    }
    private LanguageResponse map(Language l) { return new LanguageResponse(l.getId(), l.getName(), l.getCode()); }
}
