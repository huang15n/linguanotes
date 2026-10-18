package com.linguanotes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConfirmImportRequest(Long courseId, @Size(max = 180) String courseTitle,
                                       Long languageId, @NotBlank @Size(max = 180) String unitTitle) { }
