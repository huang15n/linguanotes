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
