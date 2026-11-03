import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { ResearchResponse, StreamEventPayload } from "../models/research.model";

@Injectable({
  providedIn: 'root'
})
export class ResearchService {
  private readonly http = inject(HttpClient);

  askAgent(question: string): Observable<ResearchResponse> {
    const params = new HttpParams().set('question', question);
    return this.http.get<ResearchResponse>('/api/agent', { params });
  }

  streamAgent(question: string): Observable<StreamEventPayload> {
    return new Observable<StreamEventPayload>((subscriber) => {
      const controller = new AbortController();
      const encodedQuestion = encodeURIComponent(question);
      const url = `/api/agent/stream?question=${encodedQuestion}`;

      fetch(url, { signal: controller.signal })
        .then(async (response) => {
          if (!response.ok || !response.body) {
            throw new Error(`SSE request failed with status: ${response.status}`);
          }

          const reader = response.body.getReader();
          const decoder = new TextDecoder('utf-8');
          let buffer = '';

          while (true) {
            const { done, value } = await reader.read();
            if (done) break;

            buffer += decoder.decode(value, { stream: true });
            const lines = buffer.split('\n');
            buffer = lines.pop() ?? '';

            for (const line of lines) {
              const trimmed = line.trim();
              if (trimmed.startsWith('data:')) {
                const jsonStr = trimmed.replace(/^data:\s*/, '');
                try {
                  const eventPayload: StreamEventPayload = JSON.parse(jsonStr);
                  subscriber.next(eventPayload);
                } catch {
                  // Ignore partial chunk parse glitches
                }
              }
            }
          }
          subscriber.complete();
        })
        .catch((err) => {
          if (err.name !== 'AbortError') {
            subscriber.error(err);
          }
        });

      return () => {
        controller.abort();
      };
    });
  }
}