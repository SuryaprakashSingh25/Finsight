export interface DocumentSummary {
  documentTitle: string;
  year: number | null;
  chunkCount: number;
}

export interface CompanySummary {
  company: string;
  totalReports: number;
  totalChunks: number;
  reports: DocumentSummary[];
}