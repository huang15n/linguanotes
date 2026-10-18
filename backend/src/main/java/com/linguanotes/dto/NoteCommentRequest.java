package com.linguanotes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteCommentRequest(@NotBlank @Size(max = 4000) String content) { }
