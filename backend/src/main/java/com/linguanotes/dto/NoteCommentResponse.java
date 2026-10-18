package com.linguanotes.dto;

import java.time.LocalDateTime;

public record NoteCommentResponse(Long id,
                                  Long noteId,
                                  String content,
                                  Long authorId,
                                  String authorName,
                                  boolean owned,
                                  LocalDateTime createdAt,
                                  LocalDateTime updatedAt) { }
