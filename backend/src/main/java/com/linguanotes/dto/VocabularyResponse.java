package com.linguanotes.dto;

import java.util.*;

public record VocabularyResponse(Long id, Long lessonId, Long sourceLanguageId, String sourceLanguageName,
                                     Long targetLanguageId, String targetLanguageName, String sourceText,
                                     String translation, String ipa, String partOfSpeech, List<String> examples,
                                     String personalNotes, int difficulty, List<String> tags, Long audioFileId) { }
