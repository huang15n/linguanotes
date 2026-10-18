package com.linguanotes.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linguanotes.dto.*;
import com.linguanotes.model.*;
import com.linguanotes.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ExerciseService {
    private final ExerciseRepository exercises;
    private final ExerciseAttemptRepository attempts;
    private final UserAccountRepository accounts;
    private final LessonRepository lessons;
    private final ObjectMapper json;

    public ExerciseService(ExerciseRepository exercises, ExerciseAttemptRepository attempts,
                           UserAccountRepository accounts, LessonRepository lessons, ObjectMapper json) {
        this.exercises = exercises;
        this.attempts = attempts;
        this.accounts = accounts;
        this.lessons = lessons;
        this.json = json;
    }

    @Transactional(readOnly = true)
    public List<ExerciseResponse> list(Long ownerId) {
        return exercises.findAllByOwnerIdOrderByCreatedAtDesc(ownerId).stream().map(this::map).toList();
    }

    public ExerciseResponse create(Long ownerId, ExerciseRequest request) {
        Exercise exercise = new Exercise();
        exercise.setOwner(accounts.getReferenceById(ownerId));
        apply(exercise, ownerId, request);
        return map(exercises.save(exercise));
    }

    public ExerciseResponse update(Long ownerId, Long id, ExerciseRequest request) {
        Exercise exercise = findExercise(ownerId, id);
        apply(exercise, ownerId, request);
        return map(exercises.save(exercise));
    }

    public void delete(Long ownerId, Long id) {
        exercises.delete(findExercise(ownerId, id));
    }

    @Transactional(readOnly = true)
    public ExerciseResponse get(Long ownerId, Long id) {
        return map(findExercise(ownerId, id));
    }

    public ExerciseAttemptResponse submit(Long ownerId, Long exerciseId, ExerciseAttemptRequest request) {
        Exercise exercise = findExercise(ownerId, exerciseId);
        JsonNode questions = read(exercise.getQuestionsJson());
        if (!questions.isArray() || questions.isEmpty() || request.answers().size() != questions.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nombre de réponses ne correspond pas au quiz" );
        }
        List<QuestionResult> results = new ArrayList<>();
        int possible = 0;
        int earned = 0;
        for (int i = 0; i < questions.size(); i++) {
            JsonNode question = questions.get(i);
            int points = Math.max(1, question.path("points").asInt(1));
            String expected = question.path("answer").asText();
            String submitted = request.answers().get(i);
            boolean correct = normalize(expected).equals(normalize(submitted));
            possible += points;
            int pointsEarned = correct ? points : 0;
            earned += pointsEarned;
            results.add(new QuestionResult(i, submitted, expected, correct,
                    question.path("explanation").asText(""), pointsEarned, points));
        }
        BigDecimal score = BigDecimal.valueOf(earned * 100.0 / possible).setScale(2, RoundingMode.HALF_UP);
        ExerciseAttempt attempt = new ExerciseAttempt();
        attempt.setExercise(exercise);
        attempt.setUser(accounts.getReferenceById(ownerId));
        attempt.setScore(score);
        attempt.setAnswersJson(write(request.answers()));
        attempt = attempts.save(attempt);
        return new ExerciseAttemptResponse(attempt.getId(), exerciseId, score, results, attempt.getAttemptedAt());
    }

    @Transactional(readOnly = true)
    public List<ExerciseAttemptSummary> attempts(Long ownerId, Long exerciseId) {
        Exercise exercise = findExercise(ownerId, exerciseId);
        return attempts.findAllByExerciseIdAndUserIdOrderByAttemptedAtDesc(exerciseId, ownerId).stream()
                .map(attempt -> new ExerciseAttemptSummary(attempt.getId(), exerciseId, exercise.getTitle(),
                        attempt.getScore(), attempt.getAttemptedAt()))
                .toList();
    }

    private void apply(Exercise exercise, Long ownerId, ExerciseRequest request) {
        exercise.setTitle(request.title().trim());
        exercise.setInstructions(request.instructions().trim());
        exercise.setType(request.type());
        exercise.setLesson(request.lessonId() == null ? null : ownedLesson(ownerId, request.lessonId()));
        exercise.setQuestionsJson(write(request.questions()));
        exercise.setExplanationsJson(write(request.questions().stream()
                .map(ExerciseQuestionRequest::explanation).toList()));
    }

    private Exercise findExercise(Long ownerId, Long exerciseId) {
        return exercises.findByIdAndOwnerId(exerciseId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercice introuvable"));
    }

    private Lesson ownedLesson(Long ownerId, Long lessonId) {
        Lesson lesson = lessons.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leçon introuvable"));
        if (!lesson.getUnit().getCourse().getOwner().getId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leçon introuvable");
        }
        return lesson;
    }

    private ExerciseResponse map(Exercise exercise) {
        JsonNode questions = read(exercise.getQuestionsJson());
        List<ExerciseQuestionView> views = new ArrayList<>();
        if (questions.isArray()) {
                questions.forEach(question -> views.add(new ExerciseQuestionView(
                    question.path("prompt").asText(),
                    question.path("options").isArray() ? json.convertValue(question.path("options"), new TypeReference<List<String>>() { }) : List.of(),
                    Math.max(1, question.path("points").asInt(1)))));
        }
        return new ExerciseResponse(exercise.getId(), exercise.getLesson() == null ? null : exercise.getLesson().getId(),
                exercise.getTitle(), exercise.getInstructions(), exercise.getType(), views, exercise.getCreatedAt());
    }

    private String normalize(String answer) {
        return answer == null ? "" : answer.trim().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT);
    }

    private String write(Object value) {
        try {
            return json.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Impossible d’enregistrer les données du quiz", e);
        }
    }

    private JsonNode read(String value) {
        try {
            return json.readTree(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Données du quiz invalides", e);
        }
    }
}