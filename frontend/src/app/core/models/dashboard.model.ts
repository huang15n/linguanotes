import { Note } from "./note.model";
import { TestPreparation } from "./test-preparation.model";

export interface Dashboard {
  totalNotes: number;
  totalLanguages: number;
  favorites: number;
  vocabularyCount: number;
  dueFlashcards: number;
  exerciseAttempts: number;
  averageScore: number;
  notesByType: Partial<Record<string, number>>;
  recent: Note[];
  testPreparationCount: number;
  testPreparationFavorites: number;
  testPrepByModule: Partial<Record<string, number>>;
  recentTestPreparations: TestPreparation[];
}
