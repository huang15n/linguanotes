package com.linguanotes.dto;

import com.linguanotes.model.CourseStatus;
import java.time.LocalDateTime;

public record CourseResponse(Long id, String title, Long languageId, String languageName,
                                 String level, String description, CourseStatus status,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) { }
