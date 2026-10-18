package com.linguanotes.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.*;

public record VocabularyRequest(Long lessonId, @NotNull Long sourceLanguageId,
                                    @NotNull Long targetLanguageId, @NotBlank @Size(max = 500) String sourceText,
                                    @NotBlank @Size(max = 1000) String translation, @Size(max = 255) String ipa,
                                    @Size(max = 80) String partOfSpeech, List<@Size(max = 1000) String> examples,
                                    String personalNotes, @Min(1) @Max(5) Integer difficulty, List<String> tags) { }
