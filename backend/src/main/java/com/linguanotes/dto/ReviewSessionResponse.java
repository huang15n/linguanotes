package com.linguanotes.dto;

import com.linguanotes.model.ReviewMode;
import java.time.LocalDateTime;

public record ReviewSessionResponse(Long id,
                                    ReviewMode mode,
                                    Long languageId,
                                    int totalWords,
                                    int correctCount,
                                    int incorrectCount,
                                    LocalDateTime startedAt,
                                    LocalDateTime completedAt) { }
