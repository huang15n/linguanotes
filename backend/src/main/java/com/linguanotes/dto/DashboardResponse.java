package com.linguanotes.dto;

import java.util.*;

public record DashboardResponse(long totalNotes, long totalLanguages, long favorites,
                                    Map<String, Long> notesByType, List<NoteResponse> recent) { }
