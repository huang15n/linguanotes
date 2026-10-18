package com.linguanotes.dto;

import java.time.LocalDateTime;

public record ReviewCardResponse(Long flashcardId, Long lessonId, String frontText,
                                     String backText, int repetitions, int correctCount,
                                     int incorrectCount, LocalDateTime nextDueAt) { }
