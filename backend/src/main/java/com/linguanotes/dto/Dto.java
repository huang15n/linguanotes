package com.linguanotes.dto;

import com.linguanotes.model.NoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.*;

public final class Dto {
    private Dto() { }
    public record LanguageRequest(@NotBlank String name, @NotBlank @Size(max = 10) String code) { }
    public record LanguageResponse(Long id, String name, String code) { }
    public record NoteRequest(@NotBlank @Size(max = 255) String title, String content, Long languageId,
                              NoteType type, String unit, Set<String> tags, Boolean favorite) { }
    public record NoteResponse(Long id, String title, String content, Long languageId, String languageName,
                               NoteType type, String unit, List<String> tags, boolean favorite,
                               LocalDateTime createdAt, LocalDateTime updatedAt) { }
    public record DashboardResponse(long totalNotes, long totalLanguages, long favorites,
                                    Map<String, Long> notesByType, List<NoteResponse> recent) { }
}
