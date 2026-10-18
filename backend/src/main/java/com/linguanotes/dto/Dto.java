package com.linguanotes.dto;

import com.linguanotes.model.NoteType;
import com.linguanotes.model.CourseStatus;
import com.linguanotes.model.LessonType;
import com.linguanotes.model.ExerciseType;
import com.linguanotes.model.ReviewRating;
import com.linguanotes.model.AccountRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.*;
import java.math.BigDecimal;

public final class Dto {
    private Dto() { }
    public record LanguageRequest(@NotBlank String name, @NotBlank @Size(max = 10) String code) { }
    public record LanguageResponse(Long id, String name, String code) { }
    public record NoteRequest(@NotBlank @Size(max = 255) String title, String content, Long languageId, Long lessonId,
                              NoteType type, String unit, Set<String> tags, Boolean favorite) { }
    public record NoteResponse(Long id, String title, String content, Long languageId, String languageName, Long lessonId,
                               NoteType type, String unit, List<String> tags, boolean favorite,
                               LocalDateTime createdAt, LocalDateTime updatedAt) { }
    public record DashboardResponse(long totalNotes, long totalLanguages, long favorites,
                                    Map<String, Long> notesByType, List<NoteResponse> recent) { }
    public record RegisterRequest(@NotBlank @Size(max = 120) String name,
                                  @NotBlank @Email @Size(max = 254) String email,
                                  @NotBlank @Size(min = 12, max = 64) String password) { }
    public record LoginRequest(@NotBlank @Email @Size(max = 254) String email,
                               @NotBlank String password) { }
    public record AccountResponse(Long id, String name, String email, String role) { }
    public record DeleteAccountRequest(@NotBlank String password) { }
    public record RoleUpdateRequest(@NotNull AccountRole role) { }
    public record PreferenceRequest(@NotBlank @Size(max = 10) String interfaceLocale,
                                    @Min(1) @Max(240) Integer dailyGoalMinutes) { }
    public record UserPreferenceResponse(String interfaceLocale, int dailyGoalMinutes, boolean customized) { }
    public record ProfileUpdateRequest(@NotBlank @Size(max = 120) String name) { }
    public record ChangePasswordRequest(@NotBlank String currentPassword,
                                        @NotBlank @Size(min = 12, max = 64) String newPassword) { }
    public record PasswordResetRequest(@NotBlank @Email @Size(max = 254) String email) { }
    public record PasswordResetConfirmRequest(@NotBlank String token,
                                               @NotBlank @Size(min = 12, max = 64) String password) { }
    public record CourseRequest(@NotBlank @Size(max = 180) String title, @NotNull Long languageId,
                                @Size(max = 40) String level, String description, CourseStatus status) { }
    public record CourseResponse(Long id, String title, Long languageId, String languageName,
                                 String level, String description, CourseStatus status,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) { }
    public record UnitRequest(@NotBlank @Size(max = 180) String title, int position, String description) { }
    public record UnitResponse(Long id, Long courseId, String title, int position, String description) { }
    public record LessonRequest(@NotBlank @Size(max = 180) String title, String content,
                                LessonType type, int position) { }
    public record LessonResponse(Long id, Long unitId, String title, String content,
                                 LessonType type, int position) { }
    public record VocabularyRequest(Long lessonId, @NotNull Long sourceLanguageId,
                                    @NotNull Long targetLanguageId, @NotBlank @Size(max = 500) String sourceText,
                                    @NotBlank @Size(max = 1000) String translation, @Size(max = 255) String ipa,
                                    @Size(max = 80) String partOfSpeech, List<@Size(max = 1000) String> examples,
                                    String personalNotes, @Min(1) @Max(5) Integer difficulty, List<String> tags) { }
    public record VocabularyResponse(Long id, Long lessonId, Long sourceLanguageId, String sourceLanguageName,
                                     Long targetLanguageId, String targetLanguageName, String sourceText,
                                     String translation, String ipa, String partOfSpeech, List<String> examples,
                                     String personalNotes, int difficulty, List<String> tags, Long audioFileId) { }
    public record ExerciseQuestionRequest(@NotBlank String prompt, List<String> options,
                                         @NotBlank String answer, String explanation,
                                         @Min(1) @Max(100) Integer points) { }
    public record ExerciseRequest(@NotBlank @Size(max = 180) String title, @NotBlank String instructions,
                                  Long lessonId, @NotNull ExerciseType type,
                                  @NotEmpty List<@Valid ExerciseQuestionRequest> questions) { }
    public record ExerciseQuestionView(String prompt, List<String> options, int points) { }
    public record ExerciseResponse(Long id, Long lessonId, String title, String instructions,
                                   ExerciseType type, List<ExerciseQuestionView> questions,
                                   LocalDateTime createdAt) { }
    public record ExerciseAttemptRequest(@NotEmpty List<String> answers) { }
    public record QuestionResult(int index, String submittedAnswer, String expectedAnswer,
                                 boolean correct, String explanation, int pointsEarned, int pointsPossible) { }
    public record ExerciseAttemptResponse(Long id, Long exerciseId, BigDecimal score,
                                          List<QuestionResult> results, LocalDateTime attemptedAt) { }
    public record ExerciseAttemptSummary(Long id, Long exerciseId, String exerciseTitle,
                                         BigDecimal score, LocalDateTime attemptedAt) { }
    public record FlashcardRequest(Long lessonId, Long vocabularyEntryId,
                                  @NotBlank String frontText, @NotBlank String backText) { }
    public record FlashcardResponse(Long id, Long lessonId, Long vocabularyEntryId,
                                    String frontText, String backText) { }
    public record ReviewCardResponse(Long flashcardId, Long lessonId, String frontText,
                                     String backText, int repetitions, int correctCount,
                                     int incorrectCount, LocalDateTime nextDueAt) { }
    public record ReviewAnswerRequest(@NotNull ReviewRating rating) { }
    public record ReviewResult(Long flashcardId, ReviewRating rating, int intervalDays,
                               LocalDateTime nextDueAt) { }
    public record ProgressSummary(long courseCount, long noteCount, long vocabularyCount,
                                  long exerciseCount, long attemptCount, long dueFlashcards,
                                  BigDecimal averageScore) { }
    public record ProgressPoint(java.time.LocalDate date, long attempts, BigDecimal averageScore) { }
    public record StoredFileResponse(Long id, String originalName, String contentType, long fileSize,
                                     String fileKind, LocalDateTime createdAt) { }
    public record ImportJobResponse(Long id, String originalName, String status, String preview,
                                    String errorMessage, Long courseId, LocalDateTime createdAt) { }
    public record ConfirmImportRequest(Long courseId, @Size(max = 180) String courseTitle,
                                       Long languageId, @NotBlank @Size(max = 180) String unitTitle) { }
    public record AiRequest(@NotBlank @Size(max = 10000) String content,
                            @Size(max = 100) String targetLanguage,
                            @Size(max = 40) String level) { }
    public record AiGenerationResponse(Long id, String generationType, String status,
                                       String resultContent, int remainingDailyRequests) { }
}
