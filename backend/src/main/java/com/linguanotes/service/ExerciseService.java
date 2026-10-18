package com.linguanotes.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linguanotes.dto.ExerciseAttemptRequest;
import com.linguanotes.dto.ExerciseAttemptResponse;
import com.linguanotes.dto.ExerciseAttemptSummary;
import com.linguanotes.dto.ExerciseQuestionRequest;
import com.linguanotes.dto.ExerciseQuestionView;
import com.linguanotes.dto.ExerciseRequest;
import com.linguanotes.dto.ExerciseResponse;
import com.linguanotes.dto.QuestionResult;
import com.linguanotes.model.Exercise;
import com.linguanotes.model.ExerciseAttempt;
import com.linguanotes.model.ExerciseStatus;
import com.linguanotes.model.Language;
import com.linguanotes.model.Lesson;
import com.linguanotes.repository.ExerciseAttemptRepository;
import com.linguanotes.repository.ExerciseRepository;
import com.linguanotes.repository.LanguageRepository;
import com.linguanotes.repository.LessonRepository;
import com.linguanotes.repository.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ExerciseService {
    private final ExerciseRepository exercises;
    private final ExerciseAttemptRepository attempts;
    private final UserAccountRepository accounts;
    private final LessonRepository lessons;
    private final LanguageRepository languages;
    private final ObjectMapper json;

    public ExerciseService(ExerciseRepository exercises, ExerciseAttemptRepository attempts,
                           UserAccountRepository accounts, LessonRepository lessons,
                           LanguageRepository languages, ObjectMapper json) {
        this.exercises = exercises;
        this.attempts = attempts;
        this.accounts = accounts;
        this.lessons = lessons;
        this.languages = languages;
        this.json = json;
    }

    @Transactional(readOnly = true)
    public List<ExerciseResponse> list(Long ownerId) {
        return exercises.findAllByStatusOrOwnerIdOrderByCreatedAtDesc(ExerciseStatus.PUBLISHED, ownerId)
                .stream().map(this::map).toList();
    }

    public ExerciseResponse create(Long ownerId, ExerciseRequest request) {
        Exercise exercise = new Exercise();
        exercise.setOwner(accounts.getReferenceById(ownerId));
        apply(exercise, ownerId, request);
        return map(exercises.saveAndFlush(exercise));
    }

    public ExerciseResponse update(Long ownerId, Long id, ExerciseRequest request) {
        Exercise exercise = findOwned(ownerId, id);
        apply(exercise, ownerId, request);
        return map(exercises.saveAndFlush(exercise));
    }

    public void delete(Long ownerId, Long id) {
        exercises.delete(findOwned(ownerId, id));
    }

    @Transactional(readOnly = true)
    public ExerciseResponse get(Long ownerId, Long id) {
        return map(findReadable(ownerId, id));
    }

    public ExerciseAttemptResponse submit(Long ownerId, Long exerciseId, ExerciseAttemptRequest request) {
        Exercise exercise = findReadable(ownerId, exerciseId);
        boolean preview = Boolean.TRUE.equals(request.preview());
        long completed = attempts.countByExerciseIdAndUserIdAndPreviewFalse(exerciseId, ownerId);
        if (!preview && exercise.getMaxAttempts() != null && completed >= exercise.getMaxAttempts()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Le nombre maximal de tentatives est atteint pour cet exercice");
        }
        JsonNode questions = read(exercise.getQuestionsJson());
        if (!questions.isArray() || questions.isEmpty() || request.answers().size() != questions.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le nombre de réponses ne correspond pas au quiz");
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
        BigDecimal score = BigDecimal.valueOf(earned * 100.0 / possible)
                .setScale(2, RoundingMode.HALF_UP);
        ExerciseAttempt attempt = new ExerciseAttempt();
        attempt.setExercise(exercise);
        attempt.setUser(accounts.getReferenceById(ownerId));
        attempt.setScore(score);
        attempt.setAnswersJson(write(request.answers()));
        attempt.setPreview(preview);
        attempt = attempts.saveAndFlush(attempt);
        boolean passed = score.compareTo(BigDecimal.valueOf(exercise.getPassThreshold())) >= 0;
        return new ExerciseAttemptResponse(attempt.getId(), exerciseId, score,
                exercise.getPassThreshold(), passed, preview,
                exercise.isShowAnswers() ? results : List.of(), attempt.getAttemptedAt());
    }

    @Transactional(readOnly = true)
    public List<ExerciseAttemptSummary> attempts(Long ownerId, Long exerciseId) {
        Exercise exercise = findReadable(ownerId, exerciseId);
        return attempts.findAllByExerciseIdAndUserIdOrderByAttemptedAtDesc(exerciseId, ownerId).stream()
                .map(attempt -> new ExerciseAttemptSummary(attempt.getId(), exerciseId, exercise.getTitle(),
                        attempt.getScore(), attempt.isPreview(), attempt.getAttemptedAt()))
                .toList();
    }

    private void apply(Exercise exercise, Long ownerId, ExerciseRequest request) {
        exercise.setTitle(request.title().trim());
        exercise.setInstructions(request.instructions().trim());
        exercise.setLevel(request.level());
        exercise.setGrammarTopic(request.grammarTopic());
        exercise.setIntroduction(request.introduction());
        exercise.setType(request.type());
        exercise.setStatus(request.status() == null ? ExerciseStatus.DRAFT : request.status());
        exercise.setMaxAttempts(request.maxAttempts());
        exercise.setPassThreshold(request.passThreshold() == null ? 60 : request.passThreshold());
        exercise.setShowAnswers(request.showAnswers() == null || request.showAnswers());
        exercise.setLanguage(request.languageId() == null ? null : language(request.languageId()));
        exercise.setLesson(request.lessonId() == null ? null : ownedLesson(ownerId, request.lessonId()));
        exercise.setQuestionsJson(write(request.questions()));
        exercise.setExplanationsJson(write(request.questions().stream()
                .map(ExerciseQuestionRequest::explanation).toList()));
    }

    private Exercise findOwned(Long ownerId, Long exerciseId) {
        return exercises.findByIdAndOwnerId(exerciseId, ownerId)
                .orElseThrow(this::notFound);
    }

    private Exercise findReadable(Long ownerId, Long exerciseId) {
        Exercise exercise = exercises.findById(exerciseId).orElseThrow(this::notFound);
        boolean readable = exercise.getOwner().getId().equals(ownerId)
                || exercise.getStatus() == ExerciseStatus.PUBLISHED;
        if (!readable) throw notFound();
        return exercise;
    }

    private Language language(Long id) {
        return languages.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Langue introuvable"));
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
                    question.path("options").isArray()
                            ? json.convertValue(question.path("options"), new TypeReference<List<String>>() { })
                            : List.of(),
                    Math.max(1, question.path("points").asInt(1)))));
        }
        return new ExerciseResponse(
                exercise.getId(),
                exercise.getOwner().getId(),
                exercise.getOwner().getName(),
                exercise.getLesson() == null ? null : exercise.getLesson().getId(),
                exercise.getLanguage() == null ? null : exercise.getLanguage().getId(),
                exercise.getLanguage() == null ? null : exercise.getLanguage().getName(),
                exercise.getTitle(),
                exercise.getLevel(),
                exercise.getGrammarTopic(),
                exercise.getIntroduction(),
                exercise.getInstructions(),
                exercise.getType(),
                exercise.getStatus(),
                exercise.getMaxAttempts(),
                exercise.getPassThreshold(),
                exercise.isShowAnswers(),
                views,
                exercise.getCreatedAt(),
                exercise.getUpdatedAt());
    }

    private String normalize(String answer) {
        return answer == null ? ""
                : answer.trim().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT);
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

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercice introuvable");
    }
}
