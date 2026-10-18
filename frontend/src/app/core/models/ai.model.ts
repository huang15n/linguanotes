export interface AiGeneration {
  id: number;
  generationType: string;
  status: string;
  resultContent: string;
  remainingDailyRequests: number;
}
