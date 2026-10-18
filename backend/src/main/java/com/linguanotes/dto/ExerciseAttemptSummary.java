package com.linguanotes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExerciseAttemptSummary(Long id, Long exerciseId, String exerciseTitle,
                                         BigDecimal score, LocalDateTime attemptedAt) { }
