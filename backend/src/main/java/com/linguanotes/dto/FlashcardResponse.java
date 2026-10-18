package com.linguanotes.dto;

public record FlashcardResponse(Long id, Long lessonId, Long vocabularyEntryId,
                                    String frontText, String backText) { }
