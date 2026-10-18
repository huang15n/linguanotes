package com.linguanotes.service;

import com.linguanotes.dto.AccountResponse;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AccountExportService {
    private final AccountService accounts;
    private final NoteService notes;
    private final CourseService courses;
    private final VocabularyService vocabulary;
    private final ExerciseService exercises;
    private final ReviewService reviews;
    private final FileStorageService files;
    private final PreferenceService preferences;

    public AccountExportService(AccountService accounts, NoteService notes, CourseService courses,
                                VocabularyService vocabulary, ExerciseService exercises,
                                ReviewService reviews, FileStorageService files, PreferenceService preferences) {
        this.accounts = accounts;
        this.notes = notes;
        this.courses = courses;
        this.vocabulary = vocabulary;
        this.exercises = exercises;
        this.reviews = reviews;
        this.files = files;
        this.preferences = preferences;
    }

    public Map<String, Object> export(Long userId) {
        AccountResponse account = accounts.getAccount(userId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("exportedAt", LocalDateTime.now());
        result.put("account", account);
        result.put("preferences", preferences.get(userId));
        result.put("notes", notes.search(userId, null, null, null, null, null, null));
        result.put("courses", courses.listCourses(userId));
        result.put("vocabulary", vocabulary.list(userId));
        result.put("exercises", exercises.list(userId));
        result.put("flashcards", reviews.listCards(userId));
        result.put("files", files.list(userId));
        return result;
    }
}