package com.linguanotes.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linguanotes.dto.ConjugationTable;
import com.linguanotes.dto.VocabularyRequest;
import com.linguanotes.dto.VocabularyResponse;
import com.linguanotes.model.LearningStatus;
import com.linguanotes.model.Language;
import com.linguanotes.model.Lesson;
import com.linguanotes.model.Tag;
import com.linguanotes.model.VocabularyEntry;
import com.linguanotes.repository.LanguageRepository;
import com.linguanotes.repository.LessonRepository;
import com.linguanotes.repository.TagRepository;
import com.linguanotes.repository.UserAccountRepository;
import com.linguanotes.repository.VocabularyEntryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class VocabularyService {
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() { };
    private static final TypeReference<List<ConjugationTable>> CONJUGATIONS = new TypeReference<>() { };

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
        return map(entries.saveAndFlush(entry));
    }

    public VocabularyResponse update(Long ownerId, Long id, VocabularyRequest request) {
        VocabularyEntry entry = find(ownerId, id);
        apply(entry, ownerId, request);
        return map(entries.saveAndFlush(entry));
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
        entry.setGrammaticalGender(request.grammaticalGender());
        entry.setLevel(request.level());
        entry.setDefinition(request.definition());
        entry.setExamplesJson(writeList(request.examples()));
        entry.setSynonymsJson(writeList(request.synonyms()));
        entry.setAntonymsJson(writeList(request.antonyms()));
        entry.setPersonalNotes(request.personalNotes());
        entry.setDifficulty(request.difficulty() == null ? 1 : request.difficulty());
        entry.setLearningStatus(request.learningStatus() == null ? LearningStatus.NEW : request.learningStatus());
        entry.setRegion(request.region());
        entry.setPluralForm(request.pluralForm());
        entry.setConjugationJson(write(request.conjugations() == null ? List.of() : request.conjugations()));
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

    private String writeList(List<String> values) {
        return write(values == null ? List.of() : values);
    }

    private String write(Object value) {
        try {
            return json.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Impossible d’enregistrer la fiche de vocabulaire", exception);
        }
    }

    private List<String> readList(String value) {
        if (value == null || value.isBlank()) return List.of();
        try {
            return json.readValue(value, STRING_LIST);
        } catch (JsonProcessingException exception) {
            return List.of();
        }
    }

    private List<ConjugationTable> readConjugations(String value) {
        if (value == null || value.isBlank()) return List.of();
        try {
            return json.readValue(value, CONJUGATIONS);
        } catch (JsonProcessingException exception) {
            return List.of();
        }
    }

    private VocabularyResponse map(VocabularyEntry entry) {
        return new VocabularyResponse(
                entry.getId(),
                entry.getLesson() == null ? null : entry.getLesson().getId(),
                entry.getSourceLanguage().getId(),
                entry.getSourceLanguage().getName(),
                entry.getTargetLanguage().getId(),
                entry.getTargetLanguage().getName(),
                entry.getSourceText(),
                entry.getTranslation(),
                entry.getIpa(),
                entry.getPartOfSpeech(),
                entry.getGrammaticalGender(),
                entry.getLevel(),
                entry.getDefinition(),
                readList(entry.getExamplesJson()),
                readList(entry.getSynonymsJson()),
                readList(entry.getAntonymsJson()),
                entry.getPersonalNotes(),
                entry.getDifficulty(),
                entry.getLearningStatus(),
                entry.getRegion(),
                entry.getPluralForm(),
                readConjugations(entry.getConjugationJson()),
                entry.getTags().stream().map(Tag::getName).sorted().toList(),
                entry.getAudioFileId(),
                entry.getCreatedAt(),
                entry.getUpdatedAt());
    }
}
