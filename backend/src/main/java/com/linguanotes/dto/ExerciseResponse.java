package com.linguanotes.dto;

import com.linguanotes.model.ExerciseType;
import java.time.LocalDateTime;
import java.util.*;

public record ExerciseResponse(Long id, Long lessonId, String title, String instructions,
                                   ExerciseType type, List<ExerciseQuestionView> questions,
                                   LocalDateTime createdAt) { }
