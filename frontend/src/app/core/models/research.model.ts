export interface ResearchResponse{
    response: string;
    provider: string;
}

export interface ChatMessage{
    id: string;
    sender: 'user' | 'assistant';
    text: string;
    provider?: string;
    timestamp: Date;
    loading?: boolean;
}