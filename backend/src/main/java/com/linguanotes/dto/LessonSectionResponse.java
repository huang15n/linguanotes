package com.linguanotes.dto;

import com.linguanotes.model.LessonSectionType;
import java.time.LocalDateTime;

public record LessonSectionResponse(Long id,
                                    Long lessonId,
                                    LessonSectionType type,
                                    String title,
                                    String content,
                                    String contentJson,
                                    int position,
                                    boolean visible,
                                    LocalDateTime createdAt,
                                    LocalDateTime updatedAt) { }
