package com.linguanotes.dto;

public record UserPreferenceResponse(String interfaceLocale, int dailyGoalMinutes, boolean customized) { }
