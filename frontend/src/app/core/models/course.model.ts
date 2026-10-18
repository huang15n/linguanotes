export type CourseStatus = "DRAFT" | "PUBLISHED" | "ARCHIVED" | "COMPLETED";

export interface Course {
  id: number;
  title: string;
  languageId: number;
  languageName: string;
  level: string | null;
  description: string | null;
  status: CourseStatus;
}

export interface Unit {
  id: number;
  courseId: number;
  title: string;
  position: number;
  description: string | null;
}

export interface Lesson {
  id: number;
  unitId: number;
  title: string;
  content: string | null;
  type: "LESSON" | "VOCABULARY" | "DIALOGUE" | "GRAMMAR" | "EXERCISE";
  position: number;
}
