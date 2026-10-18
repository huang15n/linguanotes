package com.linguanotes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linguanotes.dto.ExerciseAttemptRequest;
import com.linguanotes.model.Exercise;
import com.linguanotes.model.ExerciseAttempt;
import com.linguanotes.model.ExerciseStatus;
import com.linguanotes.model.UserAccount;
import com.linguanotes.repository.ExerciseAttemptRepository;
import com.linguanotes.repository.ExerciseRepository;
import com.linguanotes.repository.LanguageRepository;
import com.linguanotes.repository.LessonRepository;
import com.linguanotes.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExerciseServiceTest {
    @Test
    void scoringIsComputedOnTheServerFromStoredAnswers() {
        ExerciseRepository exercises = mock(ExerciseRepository.class);
        ExerciseAttemptRepository attempts = mock(ExerciseAttemptRepository.class);
        UserAccount owner = new UserAccount();
        owner.setId(1L);
        owner.setName("Auteur");
        Exercise exercise = new Exercise();
        exercise.setId(5L);
        exercise.setOwner(owner);
        exercise.setStatus(ExerciseStatus.PUBLISHED);
        exercise.setTitle("Salutations");
        exercise.setQuestionsJson("""
                [
                  {"prompt":"Comment dire bonjour ?","options":[],"answer":"bonjour","explanation":"","points":2},
                  {"prompt":"Comment dire merci ?","options":[],"answer":"merci","explanation":"","points":1}
                ]
                """);
        when(exercises.findById(5L)).thenReturn(Optional.of(exercise));
        when(attempts.countByExerciseIdAndUserIdAndPreviewFalse(5L, 1L)).thenReturn(0L);
        when(attempts.saveAndFlush(any(ExerciseAttempt.class))).thenAnswer(invocation -> {
            ExerciseAttempt saved = invocation.getArgument(0);
            saved.setId(9L);
            return saved;
        });

        ExerciseService service = new ExerciseService(exercises, attempts,
                mock(UserAccountRepository.class), mock(LessonRepository.class),
                mock(LanguageRepository.class), new ObjectMapper());

        var result = service.submit(1L, 5L,
                new ExerciseAttemptRequest(List.of("  Bonjour ", "non"), false));

        assertEquals(new BigDecimal("66.67"), result.score());
        assertEquals(9L, result.id());
        assertTrue(result.passed());
        assertEquals(60, result.passThreshold());
        assertTrue(result.results().get(0).correct());
        assertFalse(result.results().get(1).correct());
        assertEquals(2, result.results().get(0).pointsEarned());
    }
}
