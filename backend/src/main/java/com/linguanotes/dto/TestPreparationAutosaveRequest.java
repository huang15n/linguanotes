package com.linguanotes.dto;

import jakarta.validation.constraints.NotNull;

public record TestPreparationAutosaveRequest(
        String content,
        @NotNull Integer version
) { }
