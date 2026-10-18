package com.linguanotes.dto;

import com.linguanotes.model.NoteType;
import com.linguanotes.model.NoteVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record NoteRequest(@NotBlank @Size(max = 255) String title,
                          String content,
                          Long languageId,
                          Long lessonId,
                          NoteType type,
                          @Size(max = 30) String level,
                          NoteVisibility visibility,
                          String unit,
                          Set<String> tags,
                          Boolean favorite) { }
