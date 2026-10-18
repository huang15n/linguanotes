package com.linguanotes.service;

import com.linguanotes.dto.LanguageLevelResponse;
import com.linguanotes.dto.LanguageRequest;
import com.linguanotes.dto.LanguageResponse;
import com.linguanotes.model.Language;
import com.linguanotes.repository.LanguageLevelRepository;
import com.linguanotes.repository.LanguageRepository;
import com.linguanotes.repository.NoteRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class LanguageService {
    private final LanguageRepository languages;
    private final LanguageLevelRepository levels;
    private final NoteRepository notes;

    public LanguageService(LanguageRepository languages, LanguageLevelRepository levels, NoteRepository notes) {
        this.languages = languages;
        this.levels = levels;
        this.notes = notes;
    }

    @Transactional(readOnly = true)
    public List<LanguageResponse> list() {
        return languages.findAll(Sort.by("name")).stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public List<LanguageLevelResponse> listLevels(Long languageId) {
        requireLanguage(languageId);
        return levels.findAllByLanguageIdOrderByPositionAsc(languageId).stream()
                .map(level -> new LanguageLevelResponse(level.getId(), level.getCode(),
                        level.getLabel(), level.getPosition()))
                .toList();
    }

    public LanguageResponse create(LanguageRequest request) {
        return map(languages.save(new Language(null, request.name().trim(), request.code().trim())));
    }

    public LanguageResponse update(Long id, LanguageRequest request) {
        Language language = requireLanguage(id);
        language.setName(request.name().trim());
        language.setCode(request.code().trim());
        return map(languages.save(language));
    }

    public void delete(Long id) {
        Language language = requireLanguage(id);
        if (notes.existsByLanguageId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cette langue est utilisée par des notes");
        }
        languages.delete(language);
    }

    private Language requireLanguage(Long id) {
        return languages.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Langue introuvable"));
    }

    private LanguageResponse map(Language language) {
        List<LanguageLevelResponse> languageLevels = levels
                .findAllByLanguageIdOrderByPositionAsc(language.getId()).stream()
                .map(level -> new LanguageLevelResponse(level.getId(), level.getCode(),
                        level.getLabel(), level.getPosition()))
                .toList();
        return new LanguageResponse(language.getId(), language.getName(), language.getCode(), languageLevels);
    }
}
