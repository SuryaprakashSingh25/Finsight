export interface RecentActivity {
  company: string;
  documentTitle: string;
  year: number | null;
  chunks: number;
}

export interface OverviewStats {
  companiesTracked: number;
  reportsIndexed: number;
  totalChunksIndexed: number;
  activeAiProviders: number;
  recentActivity: RecentActivity[];
}