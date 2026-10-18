package com.linguanotes.dto;

public record UserLanguageResponse(Long id,
                                   Long languageId,
                                   String languageName,
                                   String level,
                                   boolean target) { }
