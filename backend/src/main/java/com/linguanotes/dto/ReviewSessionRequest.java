package com.linguanotes.dto;

import com.linguanotes.model.ReviewMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReviewSessionRequest(Long languageId,
                                   @NotNull ReviewMode mode,
                                   @NotEmpty List<@Valid ReviewSessionItemRequest> items) { }
