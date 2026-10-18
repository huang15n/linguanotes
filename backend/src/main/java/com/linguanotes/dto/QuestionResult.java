package com.linguanotes.dto;

public record QuestionResult(int index, String submittedAnswer, String expectedAnswer,
                                 boolean correct, String explanation, int pointsEarned, int pointsPossible) { }
