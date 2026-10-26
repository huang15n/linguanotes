package com.linguanotes.dto;

import com.linguanotes.model.ExamLanguage;
import com.linguanotes.model.ExamSkillModule;
import com.linguanotes.model.ExamType;
import com.linguanotes.model.PublicationStatus;

import java.time.LocalDateTime;

public record TestPreparationResponse(
        Long id,
        String title,
        String content,
        ExamLanguage examLanguage,
        ExamType examType,
        String examLevel,
        ExamSkillModule skillModule,
        PublicationStatus publicationStatus,
        boolean favorite,
        int version,
        LocalDateTime lastSavedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long authorId,
        String authorName,
        Long audioFileId,
        String audioFileName,
        boolean owned
) { }
