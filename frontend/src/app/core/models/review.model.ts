export type ReviewRating = "AGAIN" | "HARD" | "GOOD" | "EASY";

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
