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
export class ResearchComponent{
    private readonly researchService=inject(ResearchService);

    userInput=signal('');
    isThinking=signal(false);
    messages=signal<ChatMessage[]>([
        {
            id: 'welcome',
            sender: 'assistant',
            text: 'Hello, I am FinSight. Ask me questions about your indexed annual reports. I will retrieve grounded document evidence and compute accurate financial metrics using our MCP tools.',
            provider: 'agent',
            timestamp: new Date()
        }
    ]) ;

    quickQueries = [
        'What was the revenue and operating income reported?',
        'Calculate the percentage growth of revenue across the reported periods.',
        'What are the key operational risks outlined in the report?'
    ];

    setQuery(query: string): void{
        this.userInput.set(query);
    }

    sendQuestion(): void{
        const question=this.userInput().trim();
        if(!question || this.isThinking()){
            return;
        }
        const userMsgId=crypto.randomUUID();
        const assistantMsgId=crypto.randomUUID();

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
                timestamp: new Date(),
                loading: true
            }
        ]);

        this.userInput.set('');
        this.isThinking.set(true);
        this.researchService.askAgent(question).subscribe({
            next: (res) => {
                this.messages.update((msgs) =>
                msgs.map((m) =>
                m.id===assistantMsgId
            ?{
                ...m,
                text: res.response,
                provider: res.provider,
                loading: false
            }
            : m));
            this.isThinking.set(false);
            },
            error: (err) => {
                this.messages.update((msgs) => 
                msgs.map((m) => 
                m.id===assistantMsgId
            ?{
                ...m,
                text: 'An error occurred while contacting the financial research agent. Check backend logs and MCP server connectivity.',
                provider: 'error',
                loading: false
            }
            : m));
            this.isThinking.set(false);
            }
        });
    }
}