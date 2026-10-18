package com.linguanotes.dto;

import java.time.LocalDateTime;

public record ImportJobResponse(Long id, String originalName, String status, String preview,
                                    String errorMessage, Long courseId, LocalDateTime createdAt) { }
