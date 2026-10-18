package com.linguanotes.dto;

import java.util.List;

public record VocabularyImportReport(int imported, int failed, List<String> errors) { }
