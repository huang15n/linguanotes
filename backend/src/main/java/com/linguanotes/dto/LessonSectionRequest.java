package com.linguanotes.dto;

import com.linguanotes.model.LessonSectionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LessonSectionRequest(@NotNull LessonSectionType type,
                                   @NotBlank @Size(max = 180) String title,
                                   String content,
                                   String contentJson,
                                   int position,
                                   Boolean visible) { }
