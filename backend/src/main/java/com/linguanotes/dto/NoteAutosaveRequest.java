package com.linguanotes.dto;

import jakarta.validation.constraints.Size;

public record NoteAutosaveRequest(@Size(max = 255) String title,
                                  String content,
                                  Integer version) { }
