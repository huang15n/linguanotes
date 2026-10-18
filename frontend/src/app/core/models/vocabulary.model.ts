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
  examples: string[];
  personalNotes: string | null;
  difficulty: number;
  tags: string[];
  audioFileId: number | null;
}
