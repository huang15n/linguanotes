package com.linguanotes.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linguanotes.dto.Dto.*;
import com.linguanotes.model.*;
import com.linguanotes.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Service
@Transactional
public class VocabularyService {
    private final VocabularyEntryRepository entries;
    private final UserAccountRepository accounts;
    private final LanguageRepository languages;
    private final LessonRepository lessons;
    private final TagRepository tags;
    private final ObjectMapper json;

    public VocabularyService(VocabularyEntryRepository entries, UserAccountRepository accounts,
                             LanguageRepository languages, LessonRepository lessons,
                             TagRepository tags, ObjectMapper json) {
        this.entries = entries;
        this.accounts = accounts;
        this.languages = languages;
        this.lessons = lessons;
        this.tags = tags;
        this.json = json;
    }

    @Transactional(readOnly = true)
    public List<VocabularyResponse> list(Long ownerId) {
        return entries.findAllByOwnerIdOrderByCreatedAtDesc(ownerId).stream().map(this::map).toList();
    }

    public VocabularyResponse create(Long ownerId, VocabularyRequest request) {
        VocabularyEntry entry = new VocabularyEntry();
        entry.setOwner(accounts.getReferenceById(ownerId));
        apply(entry, ownerId, request);
        return map(entries.save(entry));
    }

    public VocabularyResponse update(Long ownerId, Long id, VocabularyRequest request) {
        VocabularyEntry entry = find(ownerId, id);
        apply(entry, ownerId, request);
        return map(entries.save(entry));
    }

    public void delete(Long ownerId, Long id) {
        entries.delete(find(ownerId, id));
    }

    private void apply(VocabularyEntry entry, Long ownerId, VocabularyRequest request) {
        entry.setSourceLanguage(language(request.sourceLanguageId()));
        entry.setTargetLanguage(language(request.targetLanguageId()));
        entry.setSourceText(request.sourceText().trim());
        entry.setTranslation(request.translation().trim());
        entry.setIpa(request.ipa());
        entry.setPartOfSpeech(request.partOfSpeech());
        entry.setExamplesJson(serialize(request.examples() == null ? List.of() : request.examples()));
        entry.setPersonalNotes(request.personalNotes());
        entry.setDifficulty(request.difficulty() == null ? 1 : request.difficulty());
        entry.setLesson(request.lessonId() == null ? null : ownedLesson(ownerId, request.lessonId()));
        Set<Tag> entryTags = new HashSet<>();
        if (request.tags() != null) {
            for (String raw : request.tags()) {
                String name = raw.trim();
                if (!name.isEmpty()) entryTags.add(tags.findByNameIgnoreCase(name)
                        .orElseGet(() -> tags.save(new Tag(null, name))));
            }
        }
        entry.setTags(entryTags);
    }

    private Language language(Long id) {
        return languages.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Langue introuvable"));
    }

    private Lesson ownedLesson(Long ownerId, Long lessonId) {
        Lesson lesson = lessons.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leçon introuvable"));
        if (!lesson.getUnit().getCourse().getOwner().getId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leçon introuvable");
        }
        return lesson;
    }

    private VocabularyEntry find(Long ownerId, Long id) {
        return entries.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fiche introuvable"));
    }

    private String serialize(List<String> examples) {
        try {
            return json.writeValueAsString(examples);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Impossible d’enregistrer les exemples", e);
        }
    }

    private VocabularyResponse map(VocabularyEntry entry) {
        List<String> examples;
        try {
            examples = entry.getExamplesJson() == null ? List.of()
                    : json.readValue(entry.getExamplesJson(), new TypeReference<>() { });
        } catch (JsonProcessingException e) {
            examples = List.of();
        }
        return new VocabularyResponse(entry.getId(), entry.getLesson() == null ? null : entry.getLesson().getId(),
                entry.getSourceLanguage().getId(), entry.getSourceLanguage().getName(),
                entry.getTargetLanguage().getId(), entry.getTargetLanguage().getName(), entry.getSourceText(),
                entry.getTranslation(), entry.getIpa(), entry.getPartOfSpeech(), examples,
                entry.getPersonalNotes(), entry.getDifficulty(),
                entry.getTags().stream().map(Tag::getName).sorted().toList(), entry.getAudioFileId());
    }
}