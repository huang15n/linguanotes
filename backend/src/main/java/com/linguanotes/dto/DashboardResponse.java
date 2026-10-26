package com.linguanotes.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardResponse(
        long totalNotes,
        long totalLanguages,
        long favorites,
        long vocabularyCount,
        long dueFlashcards,
        long exerciseAttempts,
        BigDecimal averageScore,
        Map<String, Long> notesByType,
        List<NoteResponse> recent,
        long testPreparationCount,
        long testPreparationFavorites,
        Map<String, Long> testPrepByModule,
        List<TestPreparationResponse> recentTestPreparations
) { }
