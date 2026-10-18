package com.linguanotes.dto;

import com.linguanotes.model.ReviewRating;
import java.time.LocalDateTime;

public record ReviewResult(Long flashcardId, ReviewRating rating, int intervalDays,
                               LocalDateTime nextDueAt) { }
