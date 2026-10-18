package com.linguanotes.dto;

import com.linguanotes.model.ExerciseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import java.util.*;

public record ExerciseRequest(@NotBlank @Size(max = 180) String title, @NotBlank String instructions,
                                  Long lessonId, @NotNull ExerciseType type,
                                  @NotEmpty List<@Valid ExerciseQuestionRequest> questions) { }
