import { Injectable, inject } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import {
  Account,
  Dashboard,
  Language,
  LoginRequest,
  Note,
  NoteFilters,
  NoteRequest,
  RegisterRequest,
  Course,
  AiGeneration,
  DueCard,
  Exercise,
  ExerciseResult,
  Flashcard,
  ImportJob,
  Lesson,
  ProgressPoint,
  ProgressSummary,
  Unit,
  VocabularyEntry,
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
  createCourse(body: {
    title: string;
    languageId: number;
    level: string | null;
    description: string | null;
  }) {
    return this.http.post<Course>(`${this.b}/courses`, body);
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
  exercises() {
    return this.http.get<Exercise[]>(`${this.b}/exercises`);
  }
  createExercise(body: object) {
    return this.http.post<Exercise>(`${this.b}/exercises`, body);
  }
  submitExercise(id: number, answers: string[]) {
    return this.http.post<ExerciseResult>(
      `${this.b}/exercises/${id}/attempts`,
      { answers },
    );
  }
  exerciseAttempts(id: number) {
    return this.http.get<
      {
        id: number;
        exerciseId: number;
        exerciseTitle: string;
        score: number;
        attemptedAt: string;
      }[]
    >(`${this.b}/exercises/${id}/attempts`);
  }
  flashcards() {
    return this.http.get<Flashcard[]>(`${this.b}/flashcards`);
  }
  createFlashcard(body: object) {
    return this.http.post<Flashcard>(`${this.b}/flashcards`, body);
  }
  dueCards() {
    return this.http.get<DueCard[]>(`${this.b}/review/due`);
  }
  answerCard(id: number, rating: "AGAIN" | "HARD" | "GOOD" | "EASY") {
    return this.http.post(`${this.b}/review/${id}/answer`, { rating });
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
  ai(kind: "explain" | "generate-exercise" | "enrich-note", body: object) {
    return this.http.post<AiGeneration>(`${this.b}/ai/${kind}`, body);
  }
  login(body: LoginRequest) {
    return this.http.post<Account>(`${this.b}/auth/login`, body);
  }
  updateProfile(name: string) {
    return this.http.put<Account>(`${this.b}/auth/profile`, { name });
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
    return this.http.get<string[]>(`${this.b}/tags`);
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
  toggleFavorite(id: number) {
    return this.http.patch<Note>(`${this.b}/notes/${id}/favorite`, {});
  }
  deleteNote(id: number) {
    return this.http.delete<void>(`${this.b}/notes/${id}`);
  }
}
