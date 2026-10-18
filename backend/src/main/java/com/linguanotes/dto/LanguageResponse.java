package com.linguanotes.dto;

import java.util.List;

public record LanguageResponse(Long id, String name, String code, List<LanguageLevelResponse> levels) { }
