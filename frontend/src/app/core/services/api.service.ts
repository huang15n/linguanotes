import { Injectable, inject } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import {
  Account,
  AccountStatus,
  AiGeneration,
  ClassExercise,
  ClassMember,
  Course,
  Dashboard,
  DueCard,
  Exercise,
  ExerciseAttemptSummary,
  ExerciseResult,
  Flashcard,
  ImportJob,
  Language,
  Lesson,
  LessonSection,
  LoginRequest,
  Note,
  NoteComment,
  NoteFilters,
  NoteLikeResult,
  NoteRequest,
  ProfileUpdate,
  ProgressPoint,
  ProgressSummary,
  RegisterRequest,
  ReviewMode,
  ReviewRating,
  ReviewResult,
  ReviewSession,
  SchoolClass,
  StoredFile,
  Tag,
  Unit,
  UserLanguage,
  VocabularyEntry,
  TestPreparation,
  TestPreparationRequest,
  TestPreparationFilters,
} from "../models";

@Injectable({ providedIn: "root" })
export class ApiService {
  private http = inject(HttpClient);
  private b = "/api";
  csrf() {
    return this.http.get<{ token: string }>(`${this.b}/auth/csrf`);
  }
  me() {
    return this.http.get<Account>(`${this.b}/auth/me`);
  }
  register(body: RegisterRequest) {
    return this.http.post<Account>(`${this.b}/auth/register`, body);
  }
  courses() {
    return this.http.get<Course[]>(`${this.b}/courses`);
  }
  course(id: number) {
    return this.http.get<Course>(`${this.b}/courses/${id}`);
  }
  createCourse(body: {
    title: string;
    languageId: number;
    level: string | null;
    description: string | null;
    content: string | null;
    status?: Course["status"];
  }) {
    return this.http.post<Course>(`${this.b}/courses`, body);
  }
  updateCourse(
    id: number,
    body: {
      title: string;
      languageId: number;
      level: string | null;
      description: string | null;
      content: string | null;
      status: Course["status"];
    },
  ) {
    return this.http.put<Course>(`${this.b}/courses/${id}`, body);
  }
  deleteCourse(id: number) {
    return this.http.delete<void>(`${this.b}/courses/${id}`);
  }
  units(courseId: number) {
    return this.http.get<Unit[]>(`${this.b}/courses/${courseId}/units`);
  }
  createUnit(
    courseId: number,
    body: { title: string; position: number; description: string | null },
  ) {
    return this.http.post<Unit>(`${this.b}/courses/${courseId}/units`, body);
  }
  updateUnit(
    courseId: number,
    unitId: number,
    body: { title: string; position: number; description: string | null },
  ) {
    return this.http.put<Unit>(
      `${this.b}/courses/${courseId}/units/${unitId}`,
      body,
    );
  }
  deleteUnit(courseId: number, unitId: number) {
    return this.http.delete<void>(
      `${this.b}/courses/${courseId}/units/${unitId}`,
    );
  }
  lessons(unitId: number) {
    return this.http.get<Lesson[]>(`${this.b}/units/${unitId}/lessons`);
  }
  createLesson(
    unitId: number,
    body: {
      title: string;
      content: string;
      type: Lesson["type"];
      position: number;
    },
  ) {
    return this.http.post<Lesson>(`${this.b}/units/${unitId}/lessons`, body);
  }
  updateLesson(
    unitId: number,
    lessonId: number,
    body: {
      title: string;
      content: string;
      type: Lesson["type"];
      position: number;
    },
  ) {
    return this.http.put<Lesson>(
      `${this.b}/units/${unitId}/lessons/${lessonId}`,
      body,
    );
  }
  deleteLesson(unitId: number, lessonId: number) {
    return this.http.delete<void>(
      `${this.b}/units/${unitId}/lessons/${lessonId}`,
    );
  }
  lessonSections(lessonId: number) {
    return this.http.get<LessonSection[]>(
      `${this.b}/lessons/${lessonId}/sections`,
    );
  }
  createLessonSection(
    lessonId: number,
    body: {
      type: LessonSection["type"];
      title: string;
      content: string | null;
      contentJson: string | null;
      position: number;
      visible: boolean;
    },
  ) {
    return this.http.post<LessonSection>(
      `${this.b}/lessons/${lessonId}/sections`,
      body,
    );
  }
  updateLessonSection(
    lessonId: number,
    sectionId: number,
    body: {
      type: LessonSection["type"];
      title: string;
      content: string | null;
      contentJson: string | null;
      position: number;
      visible: boolean;
    },
  ) {
    return this.http.put<LessonSection>(
      `${this.b}/lessons/${lessonId}/sections/${sectionId}`,
      body,
    );
  }
  deleteLessonSection(lessonId: number, sectionId: number) {
    return this.http.delete<void>(
      `${this.b}/lessons/${lessonId}/sections/${sectionId}`,
    );
  }
  vocabulary() {
    return this.http.get<VocabularyEntry[]>(`${this.b}/vocabulary`);
  }
  createVocabulary(body: object) {
    return this.http.post<VocabularyEntry>(`${this.b}/vocabulary`, body);
  }
  updateVocabulary(id: number, body: object) {
    return this.http.put<VocabularyEntry>(`${this.b}/vocabulary/${id}`, body);
  }
  deleteVocabulary(id: number) {
    return this.http.delete<void>(`${this.b}/vocabulary/${id}`);
  }
  exportVocabularyExcel() {
    return this.http.get(`${this.b}/vocabulary/export.xlsx`, {
      responseType: "blob",
    });
  }
  importVocabularyExcel(file: File) {
    const body = new FormData();
    body.append("file", file);
    return this.http.post<{
      imported: number;
      failed: number;
      errors: string[];
    }>(`${this.b}/vocabulary/import.xlsx`, body);
  }
  exercises() {
    return this.http.get<Exercise[]>(`${this.b}/exercises`);
  }
  createExercise(body: object) {
    return this.http.post<Exercise>(`${this.b}/exercises`, body);
  }
  updateExercise(id: number, body: object) {
    return this.http.put<Exercise>(`${this.b}/exercises/${id}`, body);
  }
  deleteExercise(id: number) {
    return this.http.delete<void>(`${this.b}/exercises/${id}`);
  }
  submitExercise(id: number, answers: string[], preview = false) {
    return this.http.post<ExerciseResult>(
      `${this.b}/exercises/${id}/attempts`,
      { answers, preview },
    );
  }
  exerciseAttempts(id: number) {
    return this.http.get<ExerciseAttemptSummary[]>(
      `${this.b}/exercises/${id}/attempts`,
    );
  }
  flashcards() {
    return this.http.get<Flashcard[]>(`${this.b}/flashcards`);
  }
  createFlashcard(body: object) {
    return this.http.post<Flashcard>(`${this.b}/flashcards`, body);
  }
  updateFlashcard(id: number, body: object) {
    return this.http.put<Flashcard>(`${this.b}/flashcards/${id}`, body);
  }
  deleteFlashcard(id: number) {
    return this.http.delete<void>(`${this.b}/flashcards/${id}`);
  }
  dueCards() {
    return this.http.get<DueCard[]>(`${this.b}/review/due`);
  }
  answerCard(id: number, rating: ReviewRating) {
    return this.http.post<ReviewResult>(`${this.b}/review/${id}/answer`, {
      rating,
    });
  }
  reviewSessions() {
    return this.http.get<ReviewSession[]>(`${this.b}/review/sessions`);
  }
  recordReviewSession(body: {
    languageId: number | null;
    mode: ReviewMode;
    items: { vocabularyEntryId: number; correct: boolean }[];
  }) {
    return this.http.post<ReviewSession>(`${this.b}/review/sessions`, body);
  }
  progressSummary() {
    return this.http.get<ProgressSummary>(`${this.b}/progress/summary`);
  }
  progressHistory() {
    return this.http.get<ProgressPoint[]>(`${this.b}/progress/history`);
  }
  uploadImport(file: File) {
    const body = new FormData();
    body.append("file", file);
    return this.http.post<ImportJob>(`${this.b}/imports`, body);
  }
  imports() {
    return this.http.get<ImportJob[]>(`${this.b}/imports`);
  }
  confirmImport(id: number, body: object) {
    return this.http.post<ImportJob>(`${this.b}/imports/${id}/confirm`, body);
  }
  files() {
    return this.http.get<StoredFile[]>(`${this.b}/files`);
  }
  uploadFile(file: File) {
    const body = new FormData();
    body.append("file", file);
    return this.http.post<StoredFile>(`${this.b}/files`, body);
  }
  downloadFile(id: number) {
    return this.http.get(`${this.b}/files/${id}`, { responseType: "blob" });
  }
  deleteFile(id: number) {
    return this.http.delete<void>(`${this.b}/files/${id}`);
  }
  adminUsers(query = "", role = "", status = "") {
    let params = new HttpParams();
    if (query) params = params.set("q", query);
    if (role) params = params.set("role", role);
    if (status) params = params.set("status", status);
    return this.http.get<Account[]>(`${this.b}/admin/users`, { params });
  }
  updateUserRole(id: number, role: Account["role"]) {
    return this.http.put<Account>(`${this.b}/admin/users/${id}/role`, { role });
  }
  updateUserStatus(id: number, status: AccountStatus) {
    return this.http.put<Account>(`${this.b}/admin/users/${id}/status`, {
      status,
    });
  }
  ai(kind: "explain" | "generate-exercise" | "enrich-note", body: object) {
    return this.http.post<AiGeneration>(`${this.b}/ai/${kind}`, body);
  }
  login(body: LoginRequest) {
    return this.http.post<Account>(`${this.b}/auth/login`, body);
  }
  updateProfile(body: ProfileUpdate) {
    return this.http.put<Account>(`${this.b}/auth/profile`, body);
  }
  userLanguages() {
    return this.http.get<UserLanguage[]>(`${this.b}/profile/languages`);
  }
  saveUserLanguage(body: {
    languageId: number;
    level: string | null;
    target: boolean;
  }) {
    return this.http.post<UserLanguage>(`${this.b}/profile/languages`, body);
  }
  deleteUserLanguage(id: number) {
    return this.http.delete<void>(`${this.b}/profile/languages/${id}`);
  }
  classes() {
    return this.http.get<SchoolClass[]>(`${this.b}/classes`);
  }
  createClass(body: { name: string; description: string | null }) {
    return this.http.post<SchoolClass>(`${this.b}/classes`, body);
  }
  joinClass(joinCode: string) {
    return this.http.post<SchoolClass>(`${this.b}/classes/join`, { joinCode });
  }
  classMembers(id: number) {
    return this.http.get<ClassMember[]>(`${this.b}/classes/${id}/members`);
  }
  removeClassMember(classId: number, userId: number) {
    return this.http.delete<void>(
      `${this.b}/classes/${classId}/members/${userId}`,
    );
  }
  deleteClass(id: number) {
    return this.http.delete<void>(`${this.b}/classes/${id}`);
  }
  classExercises(id: number) {
    return this.http.get<ClassExercise[]>(`${this.b}/classes/${id}/exercises`);
  }
  assignClassExercise(id: number, exerciseId: number) {
    return this.http.post<ClassExercise>(
      `${this.b}/classes/${id}/exercises`,
      { exerciseId, dueAt: null },
    );
  }
  removeClassExercise(classId: number, assignmentId: number) {
    return this.http.delete<void>(
      `${this.b}/classes/${classId}/exercises/${assignmentId}`,
    );
  }
  changePassword(currentPassword: string, newPassword: string) {
    return this.http.put<void>(`${this.b}/auth/password`, {
      currentPassword,
      newPassword,
    });
  }
  logout() {
    return this.http.post<void>(`${this.b}/auth/logout`, {});
  }
  requestPasswordReset(email: string) {
    return this.http.post<void>(`${this.b}/auth/password-reset`, { email });
  }
  confirmPasswordReset(token: string, password: string) {
    return this.http.post<void>(`${this.b}/auth/password-reset/confirm`, {
      token,
      password,
    });
  }
  preferences() {
    return this.http.get<{
      interfaceLocale: string;
      dailyGoalMinutes: number;
      customized: boolean;
    }>(`${this.b}/auth/preferences`);
  }
  updatePreferences(interfaceLocale: string, dailyGoalMinutes: number) {
    return this.http.put<{
      interfaceLocale: string;
      dailyGoalMinutes: number;
      customized: boolean;
    }>(`${this.b}/auth/preferences`, { interfaceLocale, dailyGoalMinutes });
  }
  exportAccount() {
    return this.http.get<Record<string, unknown>>(`${this.b}/auth/export`);
  }
  deleteAccount(password: string) {
    return this.http.request<void>("DELETE", `${this.b}/auth/account`, {
      body: { password },
    });
  }
  dashboard() {
    return this.http.get<Dashboard>(`${this.b}/dashboard`);
  }
  tags() {
    return this.http.get<Tag[]>(`${this.b}/tags`);
  }
  createTag(name: string) {
    return this.http.post<Tag>(`${this.b}/tags`, { name });
  }
  updateTag(id: number, name: string) {
    return this.http.put<Tag>(`${this.b}/tags/${id}`, { name });
  }
  deleteTag(id: number) {
    return this.http.delete<void>(`${this.b}/tags/${id}`);
  }
  languages() {
    return this.http.get<Language[]>(`${this.b}/languages`);
  }
  createLanguage(body: Partial<Language>) {
    return this.http.post<Language>(`${this.b}/languages`, body);
  }
  updateLanguage(id: number, body: Partial<Language>) {
    return this.http.put<Language>(`${this.b}/languages/${id}`, body);
  }
  deleteLanguage(id: number) {
    return this.http.delete<void>(`${this.b}/languages/${id}`);
  }
  notes(f: NoteFilters = {}) {
    let p = new HttpParams();
    for (const [k, v] of Object.entries(f))
      if (v !== null && v !== undefined && v !== "" && v !== false)
        p = p.set(k, String(v));
    return this.http.get<Note[]>(`${this.b}/notes`, { params: p });
  }
  note(id: string | number) {
    return this.http.get<Note>(`${this.b}/notes/${id}`);
  }
  createNote(body: NoteRequest) {
    return this.http.post<Note>(`${this.b}/notes`, body);
  }
  updateNote(id: string | number, body: NoteRequest) {
    return this.http.put<Note>(`${this.b}/notes/${id}`, body);
  }
  autosaveNote(
    id: string | number,
    body: { title: string; content: string; version: number },
  ) {
    return this.http.put<Note>(`${this.b}/notes/${id}/autosave`, body);
  }
  toggleFavorite(id: number) {
    return this.http.patch<Note>(`${this.b}/notes/${id}/favorite`, {});
  }
  deleteNote(id: number) {
    return this.http.delete<void>(`${this.b}/notes/${id}`);
  }
  likeNote(id: number) {
    return this.http.post<NoteLikeResult>(`${this.b}/notes/${id}/like`, {});
  }
  unlikeNote(id: number) {
    return this.http.delete<NoteLikeResult>(`${this.b}/notes/${id}/like`);
  }
  noteComments(id: number) {
    return this.http.get<NoteComment[]>(`${this.b}/notes/${id}/comments`);
  }
  addNoteComment(id: number, content: string) {
    return this.http.post<NoteComment>(`${this.b}/notes/${id}/comments`, {
      content,
    });
  }
  updateNoteComment(noteId: number, commentId: number, content: string) {
    return this.http.put<NoteComment>(
      `${this.b}/notes/${noteId}/comments/${commentId}`,
      { content },
    );
  }
  deleteNoteComment(noteId: number, commentId: number) {
    return this.http.delete<void>(
      `${this.b}/notes/${noteId}/comments/${commentId}`,
    );
  }

  testPreparations(f: TestPreparationFilters = {}) {
    let p = new HttpParams();
    if (f.q) p = p.set("q", f.q);
    if (f.examLanguage) p = p.set("examLanguage", f.examLanguage);
    if (f.examType) p = p.set("examType", f.examType);
    if (f.examLevel) p = p.set("examLevel", f.examLevel);
    if (f.skillModule) p = p.set("skillModule", f.skillModule);
    if (f.favorite != null) p = p.set("favorite", String(f.favorite));
    if (f.status) p = p.set("status", f.status);
    if (f.mineOnly) p = p.set("mineOnly", "true");
    return this.http.get<TestPreparation[]>(`${this.b}/test-preparations`, { params: p });
  }
  testPreparation(id: number) {
    return this.http.get<TestPreparation>(`${this.b}/test-preparations/${id}`);
  }
  createTestPreparation(body: TestPreparationRequest) {
    return this.http.post<TestPreparation>(`${this.b}/test-preparations`, body);
  }
  updateTestPreparation(id: number, body: TestPreparationRequest) {
    return this.http.put<TestPreparation>(`${this.b}/test-preparations/${id}`, body);
  }
  autosaveTestPreparation(id: number, body: { content: string; version: number }) {
    return this.http.patch<TestPreparation>(`${this.b}/test-preparations/${id}/autosave`, body);
  }
  deleteTestPreparation(id: number) {
    return this.http.delete<void>(`${this.b}/test-preparations/${id}`);
  }

}
