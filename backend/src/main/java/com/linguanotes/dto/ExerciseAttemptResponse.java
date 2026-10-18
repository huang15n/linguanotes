package com.linguanotes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ExerciseAttemptResponse(Long id,
                                      Long exerciseId,
                                      BigDecimal score,
                                      int passThreshold,
                                      boolean passed,
                                      boolean preview,
                                      List<QuestionResult> results,
                                      LocalDateTime attemptedAt) { }
