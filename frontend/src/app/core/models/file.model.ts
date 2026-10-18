export interface StoredFile {
  id: number;
  originalName: string;
  contentType: string;
  fileSize: number;
  fileKind: "DOCUMENT" | "AUDIO" | string;
  createdAt: string;
}
