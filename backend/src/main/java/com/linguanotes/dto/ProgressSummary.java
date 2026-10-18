package com.linguanotes.dto;

import java.math.BigDecimal;

public record ProgressSummary(long courseCount, long noteCount, long vocabularyCount,
                                  long exerciseCount, long attemptCount, long dueFlashcards,
                                  BigDecimal averageScore) { }
