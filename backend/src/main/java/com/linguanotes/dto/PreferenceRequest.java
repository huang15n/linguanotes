package com.linguanotes.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PreferenceRequest(@NotBlank @Size(max = 10) String interfaceLocale,
                                    @Min(1) @Max(240) Integer dailyGoalMinutes) { }
