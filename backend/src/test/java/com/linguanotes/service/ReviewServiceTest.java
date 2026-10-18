package com.linguanotes.service;

import com.linguanotes.model.Flashcard;
import com.linguanotes.model.ReviewProgress;
import com.linguanotes.model.ReviewRating;
import com.linguanotes.repository.*;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewServiceTest {
    @Test
    void goodRatingSchedulesFirstReviewForTomorrow() {
        FlashcardRepository cards = mock(FlashcardRepository.class);
        ReviewProgressRepository progress = mock(ReviewProgressRepository.class);
        UserAccountRepository accounts = mock(UserAccountRepository.class);
        LessonRepository lessons = mock(LessonRepository.class);
        VocabularyEntryRepository vocabulary = mock(VocabularyEntryRepository.class);
        CourseRepository courses = mock(CourseRepository.class);
        NoteRepository notes = mock(NoteRepository.class);
        ExerciseRepository exercises = mock(ExerciseRepository.class);
        ExerciseAttemptRepository attempts = mock(ExerciseAttemptRepository.class);
        ReviewService service = new ReviewService(cards, progress, accounts, lessons, vocabulary,
                courses, notes, exercises, attempts);
        Flashcard card = new Flashcard();
        card.setId(6L);
        ReviewProgress schedule = new ReviewProgress();
        schedule.setFlashcard(card);
        schedule.setNextDueAt(java.time.LocalDateTime.now());
        when(cards.findByIdAndOwnerId(6L, 3L)).thenReturn(Optional.of(card));
        when(progress.findByUserIdAndFlashcardId(3L, 6L)).thenReturn(Optional.of(schedule));
        when(progress.save(any(ReviewProgress.class))).thenAnswer(call -> call.getArgument(0));

        var result = service.answer(3L, 6L, ReviewRating.GOOD);

        assertEquals(1, result.intervalDays());
        assertEquals(1, schedule.getRepetitions());
        assertEquals(1, schedule.getCorrectCount());
        assertEquals(0, schedule.getIncorrectCount());
        verify(progress).save(schedule);
    }

    @Test
    void cardOwnedByAnotherAccountIsNotFound() {
        FlashcardRepository cards = mock(FlashcardRepository.class);
        when(cards.findByIdAndOwnerId(6L, 3L)).thenReturn(Optional.empty());
        ReviewService service = new ReviewService(cards, mock(ReviewProgressRepository.class),
                mock(UserAccountRepository.class), mock(LessonRepository.class),
                mock(VocabularyEntryRepository.class), mock(CourseRepository.class),
                mock(NoteRepository.class), mock(ExerciseRepository.class),
                mock(ExerciseAttemptRepository.class));

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.answer(3L, 6L, ReviewRating.GOOD));
    }
}