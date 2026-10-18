package com.linguanotes.dto;

import com.linguanotes.model.LearningStatus;
import java.time.LocalDateTime;
import java.util.List;

public record VocabularyResponse(Long id,
                                 Long lessonId,
                                 Long sourceLanguageId,
                                 String sourceLanguageName,
                                 Long targetLanguageId,
                                 String targetLanguageName,
                                 String sourceText,
                                 String translation,
                                 String ipa,
                                 String partOfSpeech,
                                 String grammaticalGender,
                                 String level,
                                 String definition,
                                 List<String> examples,
                                 List<String> synonyms,
                                 List<String> antonyms,
                                 String personalNotes,
                                 int difficulty,
                                 LearningStatus learningStatus,
                                 String region,
                                 String pluralForm,
                                 List<ConjugationTable> conjugations,
                                 List<String> tags,
                                 Long audioFileId,
                                 LocalDateTime createdAt,
                                 LocalDateTime updatedAt) { }
