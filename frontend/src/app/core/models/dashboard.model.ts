import { Note } from "./note.model";

export interface Dashboard {
  totalNotes: number;
  totalLanguages: number;
  favorites: number;
  notesByType: Partial<Record<string, number>>;
  recent: Note[];
}
