package com.linguanotes.dto;

import java.time.LocalDateTime;

public record ClassMemberResponse(Long userId,
                                  String name,
                                  String email,
                                  String role,
                                  LocalDateTime joinedAt) { }
