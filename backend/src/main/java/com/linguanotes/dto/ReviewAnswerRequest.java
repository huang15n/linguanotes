package com.linguanotes.dto;

import com.linguanotes.model.ReviewRating;
import jakarta.validation.constraints.NotNull;

public record ReviewAnswerRequest(@NotNull ReviewRating rating) { }
