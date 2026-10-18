package com.linguanotes.dto;

import java.time.LocalDateTime;

public record ClassExerciseResponse(Long id,
                                    Long classId,
                                    Long exerciseId,
                                    String exerciseTitle,
                                    Long exerciseOwnerId,
                                    LocalDateTime assignedAt,
                                    LocalDateTime dueAt) { }
