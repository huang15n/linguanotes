package com.linguanotes.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ExerciseAttemptRequest(@NotEmpty List<String> answers, Boolean preview) { }
