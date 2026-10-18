package com.linguanotes.dto;

import com.linguanotes.model.NoteType;
import java.time.LocalDateTime;
import java.util.*;

public record NoteResponse(Long id, String title, String content, Long languageId, String languageName, Long lessonId,
                               NoteType type, String unit, List<String> tags, boolean favorite,
                               LocalDateTime createdAt, LocalDateTime updatedAt) { }
