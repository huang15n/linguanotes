export type LearningStatus = "NEW" | "LEARNING" | "MASTERED" | "TO_REVIEW";

export interface ConjugationRow {
  person: string;
  form: string;
  note: string | null;
}

export interface ConjugationTable {
  title: string;
  mood: string | null;
  tense: string | null;
  variant: string | null;
  note: string | null;
  rows: ConjugationRow[];
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
  grammaticalGender: string | null;
  level: string | null;
  definition: string | null;
  examples: string[];
  synonyms: string[];
  antonyms: string[];
  personalNotes: string | null;
  difficulty: number;
  learningStatus: LearningStatus;
  region: string | null;
  pluralForm: string | null;
  conjugations: ConjugationTable[];
  tags: string[];
  audioFileId: number | null;
  createdAt: string;
  updatedAt: string | null;
}
