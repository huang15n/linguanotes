package com.linguanotes.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.*;

public record ExerciseAttemptRequest(@NotEmpty List<String> answers) { }
