package com.linguanotes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiRequest(@NotBlank @Size(max = 10000) String content,
                            @Size(max = 100) String targetLanguage,
                            @Size(max = 40) String level) { }
