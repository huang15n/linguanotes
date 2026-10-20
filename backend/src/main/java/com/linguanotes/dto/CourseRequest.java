package com.linguanotes.dto;

import com.linguanotes.model.CourseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseRequest(@NotBlank @Size(max = 180) String title,
                            @NotNull Long languageId,
                            @Size(max = 40) String level,
                            String description,
                            String content,
                            CourseStatus status) { }
