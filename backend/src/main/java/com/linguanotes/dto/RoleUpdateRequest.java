package com.linguanotes.dto;

import com.linguanotes.model.AccountRole;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequest(@NotNull AccountRole role) { }
