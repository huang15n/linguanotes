package com.linguanotes.dto;

public record AiGenerationResponse(Long id, String generationType, String status,
                                       String resultContent, int remainingDailyRequests) { }
