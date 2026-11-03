export interface ResearchResponse {
  response: string;
  provider: string;
}

export interface StreamEventPayload {
  type: 'status' | 'token' | 'complete' | 'error';
  content: string;
  provider?: string;
}

export interface ChatMessage {
  id: string;
  sender: 'user' | 'assistant';
  text: string;
  provider?: string;
  statusText?: string;
  timestamp: Date;
  loading?: boolean;
}