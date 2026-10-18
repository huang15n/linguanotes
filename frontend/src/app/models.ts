export type CourseStatus = "DRAFT" | "PUBLISHED" | "ARCHIVED" | "COMPLETED";
export interface Course {
  id: number;
  title: string;
  languageId: number;
  languageName: string;
  level: string | null;
  description: string | null;
  status: CourseStatus;
}
export interface Unit {
  id: number;
  courseId: number;
  title: string;
  position: number;
  description: string | null;
}
export interface Lesson {
  id: number;
  unitId: number;
  title: string;
  content: string | null;
  type: "LESSON" | "VOCABULARY" | "DIALOGUE" | "GRAMMAR" | "EXERCISE";
  position: number;
}
export type NoteType =
  | "VOCABULARY"
  | "GRAMMAR"
  | "DIALOGUE"
  | "EXERCISE"
  | "PRONUNCIATION"
  | "READING"
  | "OTHER";
export const NOTE_TYPES: { value: NoteType; label: string }[] = [
  { value: "VOCABULARY", label: "Vocabulaire" },
  { value: "GRAMMAR", label: "Grammaire" },
  { value: "DIALOGUE", label: "Dialogue" },
  { value: "EXERCISE", label: "Exercice" },
  { value: "PRONUNCIATION", label: "Prononciation" },
  { value: "READING", label: "Texte de lecture" },
  { value: "OTHER", label: "Autre" },
];
export const typeLabel = (t: string) =>
  NOTE_TYPES.find((x) => x.value === t)?.label ?? t;

export interface Language {
  id: number;
  name: string;
  code: string;
}
export interface Note {
  id: number;
  title: string;
  content: string | null;
  languageId: number | null;
  languageName: string | null;
  lessonId: number | null;
  type: NoteType;
  unit: string | null;
  tags: string[];
  favorite: boolean;
  createdAt: string;
  updatedAt: string;
}
export interface NoteRequest {
  title: string;
  content: string;
  languageId: number | null;
  lessonId?: number | null;
  type: NoteType;
  unit: string | null;
  tags: string[];
  favorite: boolean;
}
export interface NoteFilters {
  q?: string | null;
  languageId?: number | null;
  type?: string | null;
  unit?: string | null;
  tag?: string | null;
  favorite?: boolean | null;
}
export interface Dashboard {
  totalNotes: number;
  totalLanguages: number;
  favorites: number;
  notesByType: Partial<Record<string, number>>;
  recent: Note[];
}
export interface Account {
  id: number;
  name: string;
  email: string;
  role: "USER" | "TEACHER" | "ADMIN";
}
export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}
export interface LoginRequest {
  email: string;
  password: string;
}
export interface VocabularyEntry {
  id: number;
  lessonId: number | null;
  sourceLanguageId: number;
  sourceLanguageName: string;
  targetLanguageId: number;
  targetLanguageName: string;
  sourceText: string;
  translation: string;
  ipa: string | null;
  partOfSpeech: string | null;
  examples: string[];
  personalNotes: string | null;
  difficulty: number;
  tags: string[];
  audioFileId: number | null;
}
export interface ExerciseQuestion {
  prompt: string;
  options: string[];
  points: number;
}
export interface Exercise {
  id: number;
  lessonId: number | null;
  title: string;
  instructions: string;
  type: string;
  questions: ExerciseQuestion[];
  createdAt: string;
}
export interface ExerciseResult {
  id: number;
  exerciseId: number;
  score: number;
  results: {
    index: number;
    submittedAnswer: string;
    expectedAnswer: string;
    correct: boolean;
    explanation: string;
    pointsEarned: number;
    pointsPossible: number;
  }[];
  attemptedAt: string;
}
export interface Flashcard {
  id: number;
  lessonId: number | null;
  vocabularyEntryId: number | null;
  frontText: string;
  backText: string;
}
export interface DueCard {
  flashcardId: number;
  lessonId: number | null;
  frontText: string;
  backText: string;
  repetitions: number;
  correctCount: number;
  incorrectCount: number;
  nextDueAt: string;
}
export interface ProgressSummary {
  courseCount: number;
  noteCount: number;
  vocabularyCount: number;
  exerciseCount: number;
  attemptCount: number;
  dueFlashcards: number;
  averageScore: number;
}
export interface ProgressPoint {
  date: string;
  attempts: number;
  averageScore: number;
}
export interface ImportJob {
  id: number;
  originalName: string;
  status: string;
  preview: string | null;
  errorMessage: string | null;
  courseId: number | null;
  createdAt: string;
}
export interface AiGeneration {
  id: number;
  generationType: string;
  status: string;
  resultContent: string;
  remainingDailyRequests: number;
}
