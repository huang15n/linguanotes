package com.linguanotes.dto;

import com.linguanotes.model.ExerciseStatus;
import com.linguanotes.model.ExerciseType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ExerciseRequest(@NotBlank @Size(max = 180) String title,
                              Long lessonId,
                              Long languageId,
                              @Size(max = 30) String level,
                              @Size(max = 180) String grammarTopic,
                              String introduction,
                              @NotBlank String instructions,
                              @NotNull ExerciseType type,
                              ExerciseStatus status,
                              @Min(1) Integer maxAttempts,
                              @Min(0) @Max(100) Integer passThreshold,
                              Boolean showAnswers,
                              @NotEmpty List<@Valid ExerciseQuestionRequest> questions) { }
