package com.linguanotes.dto;

import com.linguanotes.model.LessonType;

public record LessonResponse(Long id, Long unitId, String title, String content,
                                 LessonType type, int position) { }
