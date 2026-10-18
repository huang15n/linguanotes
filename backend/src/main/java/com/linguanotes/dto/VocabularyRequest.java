package com.linguanotes.dto;

import com.linguanotes.model.LearningStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record VocabularyRequest(Long lessonId,
                                @NotNull Long sourceLanguageId,
                                @NotNull Long targetLanguageId,
                                @NotBlank @Size(max = 500) String sourceText,
                                @NotBlank @Size(max = 1000) String translation,
                                @Size(max = 255) String ipa,
                                @Size(max = 80) String partOfSpeech,
                                @Size(max = 20) String grammaticalGender,
                                @Size(max = 30) String level,
                                String definition,
                                List<@Size(max = 200) String> examples,
                                List<@Size(max = 200) String> synonyms,
                                List<@Size(max = 200) String> antonyms,
                                String personalNotes,
                                @Min(1) @Max(5) Integer difficulty,
                                LearningStatus learningStatus,
                                @Size(max = 80) String region,
                                @Size(max = 255) String pluralForm,
                                List<@Valid ConjugationTable> conjugations,
                                List<String> tags) { }
