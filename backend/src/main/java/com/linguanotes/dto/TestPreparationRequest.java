package com.linguanotes.dto;

import com.linguanotes.model.ExamLanguage;
import com.linguanotes.model.ExamSkillModule;
import com.linguanotes.model.ExamType;
import com.linguanotes.model.NoteVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TestPreparationRequest(
        @NotBlank @Size(max = 255) String title,
        String content,
        @NotNull ExamLanguage examLanguage,
        @NotNull ExamType examType,
        @NotBlank @Size(max = 30) String examLevel,
        @NotNull ExamSkillModule skillModule,
        NoteVisibility visibility,
        Boolean favorite,
        Long audioFileId
) { }
