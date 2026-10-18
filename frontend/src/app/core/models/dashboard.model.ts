import { Note } from "./note.model";

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
}
