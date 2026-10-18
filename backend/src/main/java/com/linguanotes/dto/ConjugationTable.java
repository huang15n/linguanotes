package com.linguanotes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ConjugationTable(@NotBlank @Size(max = 120) String title,
                               @Size(max = 80) String mood,
                               @Size(max = 80) String tense,
                               @Size(max = 80) String variant,
                               @Size(max = 500) String note,
                               List<@Valid ConjugationRow> rows) { }
