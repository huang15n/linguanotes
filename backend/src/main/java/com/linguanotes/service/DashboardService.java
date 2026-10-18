package com.linguanotes.service;

import com.linguanotes.dto.DashboardResponse;
import com.linguanotes.model.Note;
import com.linguanotes.repository.ExerciseAttemptRepository;
import com.linguanotes.repository.LanguageRepository;
import com.linguanotes.repository.ReviewProgressRepository;
import com.linguanotes.repository.VocabularyEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {
    private final NoteService notes;
    private final LanguageRepository languages;
    private final VocabularyEntryRepository vocabulary;
    private final ReviewProgressRepository reviews;
    private final ExerciseAttemptRepository attempts;

    public DashboardService(NoteService notes, LanguageRepository languages,
                            VocabularyEntryRepository vocabulary, ReviewProgressRepository reviews,
                            ExerciseAttemptRepository attempts) {
        this.notes = notes;
        this.languages = languages;
        this.vocabulary = vocabulary;
        this.reviews = reviews;
        this.attempts = attempts;
    }

    public DashboardResponse dashboard(Long userId) {
        Map<String, Long> byType = notes.ownedNotes(userId).stream()
                .collect(Collectors.groupingBy(note -> note.getType().name(), Collectors.counting()));
        Double average = attempts.averageScoreByUserId(userId);
        return new DashboardResponse(
                notes.ownedNotes(userId).size(),
                languages.count(),
                notes.ownedNotes(userId).stream().filter(Note::isFavorite).count(),
                vocabulary.countByOwnerId(userId),
                reviews.countByUserIdAndNextDueAtLessThanEqual(userId, LocalDateTime.now()),
                attempts.countByUserId(userId),
                average == null ? BigDecimal.ZERO
                        : BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP),
                byType,
                notes.recentOwned(userId, 5));
    }
}
