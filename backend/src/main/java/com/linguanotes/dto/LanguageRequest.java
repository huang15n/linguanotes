package com.linguanotes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LanguageRequest(@NotBlank String name, @NotBlank @Size(max = 10) String code) { }
