package com.linguanotes.dto;

import com.linguanotes.model.NoteType;
import com.linguanotes.model.NoteVisibility;
import java.time.LocalDateTime;
import java.util.List;

public record NoteResponse(Long id,
                           String title,
                           String content,
                           Long languageId,
                           String languageName,
                           Long lessonId,
                           NoteType type,
                           String level,
                           NoteVisibility visibility,
                           Long authorId,
                           String authorName,
                           String unit,
                           List<String> tags,
                           boolean favorite,
                           int version,
                           LocalDateTime lastSavedAt,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt,
                           long likeCount,
                           long commentCount,
                           boolean likedByMe) { }
