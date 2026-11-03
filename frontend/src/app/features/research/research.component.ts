import { Component, inject, signal } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { ResearchService } from "../../core/services/research.service";
import { ChatMessage } from "../../core/models/research.model";
import { MarkdownPipe } from "../../core/pipes/markdown.pipe";

@Component({
  selector: 'app-research',
  standalone: true,
  imports: [FormsModule, MarkdownPipe],
  templateUrl: './research.component.html',
  styleUrl: './research.component.scss'
})
export class ResearchComponent {
  private readonly researchService = inject(ResearchService);

  userInput = signal('');
  isThinking = signal(false);
  messages = signal<ChatMessage[]>([
    {
      id: 'welcome',
      sender: 'assistant',
      text: 'Hello, I am FinSight. Ask me questions about your indexed annual reports. I will retrieve grounded document evidence and compute accurate financial metrics using our MCP tools.',
      provider: 'agent',
      timestamp: new Date()
    }
  ]);

  quickQueries = [
    'What was the revenue and operating income reported?',
    'Calculate the percentage growth of revenue across the reported periods.',
    'What are the key operational risks outlined in the report?'
  ];

  setQuery(query: string): void {
    this.userInput.set(query);
  }

  sendQuestion(): void {
    const question = this.userInput().trim();
    if (!question || this.isThinking()) {
      return;
    }

    const userMsgId = crypto.randomUUID();
    const assistantMsgId = crypto.randomUUID();

    this.messages.update((msgs) => [
      ...msgs,
      {
        id: userMsgId,
        sender: 'user',
        text: question,
        timestamp: new Date()
      },
      {
        id: assistantMsgId,
        sender: 'assistant',
        text: '',
        statusText: 'Analyzing context & consulting MCP tools...',
        timestamp: new Date(),
        loading: true
      }
    ]);

    this.userInput.set('');
    this.isThinking.set(true);

    this.researchService.streamAgent(question).subscribe({
      next: (event) => {
        if (event.type === 'status') {
          this.messages.update((msgs) =>
            msgs.map((m) =>
              m.id === assistantMsgId ? { ...m, statusText: event.content } : m
            )
          );
        } else if (event.type === 'token') {
          this.messages.update((msgs) =>
            msgs.map((m) =>
              m.id === assistantMsgId
                ? {
                    ...m,
                    text: m.text + event.content,
                    loading: false
                  }
                : m
            )
          );
        } else if (event.type === 'complete') {
          this.messages.update((msgs) =>
            msgs.map((m) =>
              m.id === assistantMsgId
                ? {
                    ...m,
                    provider: event.provider,
                    loading: false
                  }
                : m
            )
          );
          this.isThinking.set(false);
        } else if (event.type === 'error') {
          this.messages.update((msgs) =>
            msgs.map((m) =>
              m.id === assistantMsgId
                ? {
                    ...m,
                    text: event.content,
                    provider: 'error',
                    loading: false
                  }
                : m
            )
          );
          this.isThinking.set(false);
        }
      },
      error: (err) => {
        console.error('Streaming error:', err);
        this.messages.update((msgs) =>
          msgs.map((m) =>
            m.id === assistantMsgId
              ? {
                  ...m,
                  text: 'An error occurred while streaming response from FinSight agent. Check backend logs and MCP connection.',
                  provider: 'error',
                  loading: false
                }
              : m
          )
        );
        this.isThinking.set(false);
      }
    });
  }
}