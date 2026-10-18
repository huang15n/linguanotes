package com.linguanotes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public record ExerciseAttemptResponse(Long id, Long exerciseId, BigDecimal score,
                                          List<QuestionResult> results, LocalDateTime attemptedAt) { }
