package com.linguanotes.dto;

import com.linguanotes.model.AccountStatus;
import java.time.LocalDateTime;

public record AccountResponse(Long id,
                              String name,
                              String email,
                              String role,
                              String firstName,
                              String lastName,
                              String displayName,
                              String avatarUrl,
                              String country,
                              String timezone,
                              Long nativeLanguageId,
                              String nativeLanguageName,
                              AccountStatus status,
                              LocalDateTime lastLoginAt) { }
