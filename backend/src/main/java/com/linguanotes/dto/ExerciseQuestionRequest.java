package com.linguanotes.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.*;

public record ExerciseQuestionRequest(@NotBlank String prompt, List<String> options,
                                         @NotBlank String answer, String explanation,
                                         @Min(1) @Max(100) Integer points) { }
