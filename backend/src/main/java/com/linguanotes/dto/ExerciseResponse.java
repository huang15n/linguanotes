package com.linguanotes.dto;

import com.linguanotes.model.ExerciseStatus;
import com.linguanotes.model.ExerciseType;
import java.time.LocalDateTime;
import java.util.List;

public record ExerciseResponse(Long id,
                               Long ownerId,
                               String ownerName,
                               Long lessonId,
                               Long languageId,
                               String languageName,
                               String title,
                               String level,
                               String grammarTopic,
                               String introduction,
                               String instructions,
                               ExerciseType type,
                               ExerciseStatus status,
                               Integer maxAttempts,
                               int passThreshold,
                               boolean showAnswers,
                               List<ExerciseQuestionView> questions,
                               LocalDateTime createdAt,
                               LocalDateTime updatedAt) { }
