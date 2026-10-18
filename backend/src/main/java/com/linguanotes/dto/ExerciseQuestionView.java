package com.linguanotes.dto;

import java.util.*;

public record ExerciseQuestionView(String prompt, List<String> options, int points) { }
