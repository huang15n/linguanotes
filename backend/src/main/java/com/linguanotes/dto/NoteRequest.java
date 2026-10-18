package com.linguanotes.dto;

import com.linguanotes.model.NoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.*;

public record NoteRequest(@NotBlank @Size(max = 255) String title, String content, Long languageId, Long lessonId,
                              NoteType type, String unit, Set<String> tags, Boolean favorite) { }
