package com.linguanotes.dto;

import com.linguanotes.model.LessonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LessonRequest(@NotBlank @Size(max = 180) String title, String content,
                                LessonType type, int position) { }
