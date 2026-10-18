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

export interface ExerciseAttemptSummary {
  id: number;
  exerciseId: number;
  exerciseTitle: string;
  score: number;
  attemptedAt: string;
}
