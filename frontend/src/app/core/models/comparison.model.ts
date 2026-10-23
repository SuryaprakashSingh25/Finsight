export interface ComparisonRequest {
  companyA: string;
  yearA?: number | null;
  companyB: string;
  yearB?: number | null;
  metricFocus: string;
}

export interface ComparisonResponse {
  analysis: string;
  provider: string;
}