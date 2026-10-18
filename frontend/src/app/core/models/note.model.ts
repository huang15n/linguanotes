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

export const typeLabel = (type: string) =>
  NOTE_TYPES.find((item) => item.value === type)?.label ?? type;

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
