import type { NoteVisibility } from "./note.model";

export type ExamLanguage = "ENGLISH" | "FRENCH" | "SPANISH";
export type ExamType = "IELTS" | "TCF" | "TEF" | "DELF" | "DELE";
export type ExamSkillModule = "LISTENING" | "SPEAKING" | "READING" | "WRITING";
export type PublicationStatus = "DRAFT" | "PUBLISHED";

export const EXAM_LANGUAGES: { value: ExamLanguage; labelKey: string }[] = [
  { value: "ENGLISH", labelKey: "testPrep.lang.english" },
  { value: "FRENCH", labelKey: "testPrep.lang.french" },
  { value: "SPANISH", labelKey: "testPrep.lang.spanish" },
];

export const EXAM_TYPES_BY_LANGUAGE: Record<ExamLanguage, ExamType[]> = {
  ENGLISH: ["IELTS"],
  FRENCH: ["TCF", "TEF", "DELF"],
  SPANISH: ["DELE"],
};

export const EXAM_LEVELS_BY_TYPE: Record<ExamType, string[]> = {
  IELTS: ["6", "6.5", "7", "7.5", "8", "8.5", "9"],
  TCF: ["A1", "A2", "B1", "B2", "C1", "C2"],
  TEF: ["A1", "A2", "B1", "B2", "C1", "C2"],
  DELF: ["A1", "A2", "B1", "B2", "C1", "C2"],
  DELE: ["A1", "A2", "B1", "B2", "C1", "C2"],
};

export const EXAM_SKILL_MODULES: { value: ExamSkillModule; labelKey: string }[] = [
  { value: "LISTENING", labelKey: "testPrep.module.listening" },
  { value: "SPEAKING", labelKey: "testPrep.module.speaking" },
  { value: "READING", labelKey: "testPrep.module.reading" },
  { value: "WRITING", labelKey: "testPrep.module.writing" },
];

export interface TestPreparation {
  id: number;
  title: string;
  content: string | null;
  examLanguage: ExamLanguage;
  examType: ExamType;
  examLevel: string;
  skillModule: ExamSkillModule;
  publicationStatus: PublicationStatus;
  favorite: boolean;
  version: number;
  lastSavedAt: string | null;
  createdAt: string;
  updatedAt: string;
  authorId: number;
  authorName: string;
  audioFileId: number | null;
  audioFileName: string | null;
  owned: boolean;
}

export interface TestPreparationRequest {
  title: string;
  content: string;
  examLanguage: ExamLanguage;
  examType: ExamType;
  examLevel: string;
  skillModule: ExamSkillModule;
  publicationStatus: PublicationStatus;
  favorite: boolean;
  audioFileId: number | null;
}

export interface TestPreparationFilters {
  q?: string | null;
  examLanguage?: ExamLanguage | null;
  examType?: ExamType | null;
  examLevel?: string | null;
  skillModule?: ExamSkillModule | null;
  favorite?: boolean | null;
  status?: PublicationStatus | null;
  mineOnly?: boolean | null;
}
