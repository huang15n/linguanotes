package com.linguanotes.dto;

import jakarta.validation.constraints.NotBlank;

public record FlashcardRequest(Long lessonId, Long vocabularyEntryId,
                                  @NotBlank String frontText, @NotBlank String backText) { }
