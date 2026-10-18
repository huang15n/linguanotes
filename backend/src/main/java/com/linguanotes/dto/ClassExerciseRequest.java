package com.linguanotes.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ClassExerciseRequest(@NotNull Long exerciseId, LocalDateTime dueAt) { }
