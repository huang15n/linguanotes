package com.linguanotes.dto;

import com.linguanotes.model.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull AccountStatus status) { }
