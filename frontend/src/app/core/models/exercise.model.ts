export type ExerciseStatus = "DRAFT" | "PUBLISHED" | "ARCHIVED";

export interface ExerciseQuestion {
  prompt: string;
  options: string[];
  points: number;
}

export interface Exercise {
  id: number;
  ownerId: number;
  ownerName: string;
  lessonId: number | null;
  languageId: number | null;
  languageName: string | null;
  title: string;
  level: string | null;
  grammarTopic: string | null;
  introduction: string | null;
  instructions: string;
  type: string;
  status: ExerciseStatus;
  maxAttempts: number | null;
  passThreshold: number;
  showAnswers: boolean;
  questions: ExerciseQuestion[];
  createdAt: string;
  updatedAt: string;
}

export interface ExerciseResult {
  id: number;
  exerciseId: number;
  score: number;
  passThreshold: number;
  passed: boolean;
  preview: boolean;
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

export interface ExerciseAttemptSummary {
  id: number;
  exerciseId: number;
  exerciseTitle: string;
  score: number;
  preview: boolean;
  attemptedAt: string;
}
