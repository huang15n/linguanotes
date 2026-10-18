package com.linguanotes.dto;

import jakarta.validation.constraints.NotNull;

public record ReviewSessionItemRequest(@NotNull Long vocabularyEntryId, boolean correct) { }
