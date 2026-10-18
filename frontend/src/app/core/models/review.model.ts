export type ReviewRating = "AGAIN" | "HARD" | "GOOD" | "EASY";

export type ReviewMode =
  | "FLASHCARD"
  | "TARGET_TO_SOURCE"
  | "SOURCE_TO_TARGET"
  | "CHOICE";

export interface ReviewSession {
  id: number;
  mode: ReviewMode;
  languageId: number | null;
  totalWords: number;
  correctCount: number;
  incorrectCount: number;
  startedAt: string;
  completedAt: string | null;
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

export interface ReviewResult {
  flashcardId: number;
  rating: ReviewRating;
  intervalDays: number;
  nextDueAt: string;
}
