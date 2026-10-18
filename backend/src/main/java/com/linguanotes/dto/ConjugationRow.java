package com.linguanotes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConjugationRow(@NotBlank @Size(max = 120) String person,
                             @NotBlank @Size(max = 200) String form,
                             @Size(max = 500) String note) { }
