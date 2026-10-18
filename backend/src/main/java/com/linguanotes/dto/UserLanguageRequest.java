package com.linguanotes.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserLanguageRequest(@NotNull Long languageId,
                                  @Size(max = 30) String level,
                                  Boolean target) { }
