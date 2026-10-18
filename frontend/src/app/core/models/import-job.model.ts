export interface ImportJob {
  id: number;
  originalName: string;
  status: string;
  preview: string | null;
  errorMessage: string | null;
  courseId: number | null;
  createdAt: string;
}
