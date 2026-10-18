package com.linguanotes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(@NotBlank @Size(max = 120) String name,
                                   @Size(max = 80) String firstName,
                                   @Size(max = 80) String lastName,
                                   @Size(max = 120) String displayName,
                                   @Size(max = 500) String avatarUrl,
                                   @Size(max = 80) String country,
                                   @Size(max = 60) String timezone,
                                   Long nativeLanguageId) { }
