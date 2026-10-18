package com.linguanotes.service;

import com.linguanotes.dto.Dto.*;
import com.linguanotes.model.*;
import com.linguanotes.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReviewService {
    private final FlashcardRepository cards;
    private final ReviewProgressRepository progress;
    private final UserAccountRepository accounts;
    private final LessonRepository lessons;
    private final VocabularyEntryRepository vocabulary;
    private final CourseRepository courses;
    private final NoteRepository notes;
    private final ExerciseRepository exercises;
    private final ExerciseAttemptRepository attempts;

    public ReviewService(FlashcardRepository cards, ReviewProgressRepository progress,
                         UserAccountRepository accounts, LessonRepository lessons,
                         VocabularyEntryRepository vocabulary, CourseRepository courses,
                         NoteRepository notes, ExerciseRepository exercises, ExerciseAttemptRepository attempts) {
        this.cards = cards;
        this.progress = progress;
        this.accounts = accounts;
        this.lessons = lessons;
        this.vocabulary = vocabulary;
        this.courses = courses;
        this.notes = notes;
        this.exercises = exercises;
        this.attempts = attempts;
    }

    @Transactional(readOnly = true)
    public List<FlashcardResponse> listCards(Long ownerId) {
        return cards.findAllByOwnerIdOrderByCreatedAtDesc(ownerId).stream().map(this::map).toList();
    }

    public FlashcardResponse createCard(Long ownerId, FlashcardRequest request) {
        Flashcard card = new Flashcard();
        card.setOwner(accounts.getReferenceById(ownerId));
        card.setLesson(request.lessonId() == null ? null : ownedLesson(ownerId, request.lessonId()));
        VocabularyEntry entry = request.vocabularyEntryId() == null ? null : vocabulary.findByIdAndOwnerId(request.vocabularyEntryId(), ownerId)
                .orElseThrow(() -> notFound("Fiche de vocabulaire introuvable"));
        card.setVocabularyEntry(entry);
        card.setFrontText(entry == null ? request.frontText().trim() : entry.getSourceText());
        card.setBackText(entry == null ? request.backText().trim() : entry.getTranslation());
        card = cards.save(card);

        ReviewProgress schedule = new ReviewProgress();
        schedule.setUser(accounts.getReferenceById(ownerId));
        schedule.setFlashcard(card);
        schedule.setNextDueAt(LocalDateTime.now());
        progress.save(schedule);
        return map(card);
    }

    public void deleteCard(Long ownerId, Long id) {
        cards.delete(findCard(ownerId, id));
    }

    @Transactional(readOnly = true)
    public List<ReviewCardResponse> due(Long ownerId) {
        return progress.findAllByUserIdAndNextDueAtLessThanEqualOrderByNextDueAtAsc(ownerId, LocalDateTime.now())
                .stream().map(this::map).toList();
    }

    public ReviewResult answer(Long ownerId, Long cardId, ReviewRating rating) {
        findCard(ownerId, cardId);
        ReviewProgress schedule = progress.findByUserIdAndFlashcardId(ownerId, cardId)
                .orElseThrow(() -> notFound("Progression de révision introuvable"));
        LocalDateTime now = LocalDateTime.now();
        int repetitions = schedule.getRepetitions();
        double ease = schedule.getEaseFactor().doubleValue();
        int interval;
        switch (rating) {
            case AGAIN -> {
                interval = 1;
                schedule.setRepetitions(0);
                schedule.setIncorrectCount(schedule.getIncorrectCount() + 1);
                ease = Math.max(1.3, ease - 0.2);
            }
            case HARD -> {
                interval = Math.max(1, (int) Math.round(Math.max(1, schedule.getIntervalDays()) * 1.2));
                schedule.setRepetitions(repetitions + 1);
                schedule.setIncorrectCount(schedule.getIncorrectCount() + 1);
                ease = Math.max(1.3, ease - 0.15);
            }
            case GOOD -> {
                interval = repetitions == 0 ? 1 : repetitions == 1 ? 6
                        : Math.max(1, (int) Math.round(schedule.getIntervalDays() * ease));
                schedule.setRepetitions(repetitions + 1);
                schedule.setCorrectCount(schedule.getCorrectCount() + 1);
            }
            case EASY -> {
                interval = Math.max(4, (int) Math.round(Math.max(1, schedule.getIntervalDays()) * (ease + 0.3)));
                schedule.setRepetitions(repetitions + 1);
                schedule.setCorrectCount(schedule.getCorrectCount() + 1);
                ease += 0.15;
            }
            default -> throw new IllegalStateException("Évaluation inconnue");
        }
        schedule.setIntervalDays(interval);
        schedule.setEaseFactor(BigDecimal.valueOf(ease).setScale(2, java.math.RoundingMode.HALF_UP));
        schedule.setLastReviewedAt(now);
        schedule.setNextDueAt(now.plusDays(interval));
        progress.save(schedule);
        return new ReviewResult(cardId, rating, interval, schedule.getNextDueAt());
    }

    @Transactional(readOnly = true)
    public ProgressSummary summary(Long ownerId) {
        Double average = attempts.averageScoreByUserId(ownerId);
        return new ProgressSummary(courses.countByOwnerId(ownerId), notes.countByOwnerId(ownerId),
                vocabulary.countByOwnerId(ownerId), exercises.countByOwnerId(ownerId), attempts.countByUserId(ownerId),
                progress.countByUserIdAndNextDueAtLessThanEqual(ownerId, LocalDateTime.now()),
                average == null ? BigDecimal.ZERO : BigDecimal.valueOf(average).setScale(2, java.math.RoundingMode.HALF_UP));
    }

    @Transactional(readOnly = true)
    public List<ProgressPoint> history(Long ownerId) {
        LocalDateTime since = LocalDateTime.now().minusDays(30).toLocalDate().atStartOfDay();
        return attempts.findAllByUserIdAndAttemptedAtAfterOrderByAttemptedAtAsc(ownerId, since).stream()
                .collect(java.util.stream.Collectors.groupingBy(a -> a.getAttemptedAt().toLocalDate(),
                        java.util.TreeMap::new, java.util.stream.Collectors.toList()))
                .entrySet().stream().map(entry -> {
                    List<ExerciseAttempt> values = entry.getValue();
                    double average = values.stream().map(ExerciseAttempt::getScore).mapToDouble(BigDecimal::doubleValue).average().orElse(0);
                    return new ProgressPoint(entry.getKey(), values.size(), BigDecimal.valueOf(average).setScale(2, java.math.RoundingMode.HALF_UP));
                }).toList();
    }

    private Flashcard findCard(Long ownerId, Long cardId) {
        return cards.findByIdAndOwnerId(cardId, ownerId)
                .orElseThrow(() -> notFound("Carte introuvable"));
    }

    private Lesson ownedLesson(Long ownerId, Long lessonId) {
        Lesson lesson = lessons.findById(lessonId).orElseThrow(() -> notFound("Leçon introuvable"));
        if (!lesson.getUnit().getCourse().getOwner().getId().equals(ownerId)) throw notFound("Leçon introuvable");
        return lesson;
    }

    private FlashcardResponse map(Flashcard card) {
        return new FlashcardResponse(card.getId(), card.getLesson() == null ? null : card.getLesson().getId(),
                card.getVocabularyEntry() == null ? null : card.getVocabularyEntry().getId(),
                card.getFrontText(), card.getBackText());
    }

    private ReviewCardResponse map(ReviewProgress item) {
        Flashcard card = item.getFlashcard();
        return new ReviewCardResponse(card.getId(), card.getLesson() == null ? null : card.getLesson().getId(),
                card.getFrontText(), card.getBackText(), item.getRepetitions(), item.getCorrectCount(),
                item.getIncorrectCount(), item.getNextDueAt());
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}