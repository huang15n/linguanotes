package com.linguanotes.dto;

import java.time.LocalDateTime;

public record StoredFileResponse(Long id, String originalName, String contentType, long fileSize,
                                     String fileKind, LocalDateTime createdAt) { }
