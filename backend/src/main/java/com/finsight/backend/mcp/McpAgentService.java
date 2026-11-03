package com.finsight.backend.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class McpAgentService {

    private static final Logger log= LoggerFactory.getLogger(McpAgentService.class);

    private final List<AgentProvider> providers;

    private static final String SYSTEM_PROMPT = """
            You are FinSight, an elite financial research assistant and investment analyst.

            Guidelines:
            1. Context Priority: Use provided document context for historical GAAP figures.
            2. Strict Math via Tools: NEVER calculate yourself. Invoke MCP tools ('profitMargin', 'cagr', 'debtToEquity', 'dupontAnalysis', 'altmanZScore').
            3. Real-Time News & Live Quotes: Invoke 'getRecentCompanyNews' and 'getLiveMarketQuote'.
            4. Tool Efficiency: Issue all required tool calls together in parallel in your first turn.
            5. Output: Professional Markdown without scratchpad or LaTeX delimiters ($$ or \\\\( \\\\)).
            """;

    public McpAgentService(
            @Qualifier("openAiChatModel") ChatModel openRouterModel,
            @Qualifier("groqAgentChatModel") ChatModel groqModel,
            @Qualifier("geminiAgentChatModel") ChatModel geminiModel,
            ToolCallbackProvider mcpTools
    ) {
        this.providers=List.of(
                createProvider("openrouter",openRouterModel,mcpTools),
                createProvider("groq",groqModel,mcpTools),
                createProvider("gemini",geminiModel,mcpTools)
        );
    }

    private AgentProvider createProvider(
            String name,
            ChatModel model,
            ToolCallbackProvider mcpTools
    ){
        ChatClient client=ChatClient.builder(model)
                .defaultToolCallbacks(mcpTools)
                .build();
        return new AgentProvider(name,client);
    }

    /**
     * Non-streaming call (kept for TearSheet and Comparison workflows)
     */
    public AgentResult ask(String question) {
        RuntimeException lastException=null;

        for(AgentProvider provider:providers){
            try{
                log.info("Attemting agent request with provider={}",provider.name());
                String response=provider.chatClient()
                        .prompt()
                        .system(SYSTEM_PROMPT)
                        .user(question)
                        .call()
                        .content();

                if(response==null || response.isBlank()){
                    throw new IllegalStateException(
                            "Provider returned an empty response"
                    );
                }
                log.info("Agent request succeeded with provider={}",provider.name());
                return new AgentResult(response,provider.name());
            } catch (Exception ex){
                log.warn("Agent request failed with provider={}: {}",provider.name(),ex.getMessage());
                lastException=new RuntimeException(
                        "Provider "+provider.name()+" failed",ex);
            }
        }
        throw new IllegalStateException("All agent providers failed", lastException);
    }

    /**
     * Streaming call returning token-by-token events with fallback
     */
    public Flux<StreamEvent> streamAsk(String question) {
        return Flux.defer(() -> streamWithProviderIndex(question, 0));
    }

    private Flux<StreamEvent> streamWithProviderIndex(String question, int providerIndex) {
        if (providerIndex >= providers.size()) {
            return Flux.just(StreamEvent.error("All AI research providers failed to generate a response."));
        }

        AgentProvider provider = providers.get(providerIndex);
        log.info("Attempting agent STREAM request with provider={}", provider.name());

        Flux<StreamEvent> statusEvent = Flux.just(
                StreamEvent.status("Consulting " + provider.name().toUpperCase() + " with MCP tools...")
        );

        Flux<StreamEvent> tokenStream = provider.chatClient()
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(question)
                .stream()
                .content()
                .filter(chunk -> chunk != null && !chunk.isEmpty())
                .map(StreamEvent::token)
                .concatWith(Flux.just(StreamEvent.complete(provider.name())))
                .onErrorResume(ex -> {
                    log.warn("Streaming failed on provider={}: {}. Falling back to next provider...",
                            provider.name(), ex.getMessage());
                    return streamWithProviderIndex(question, providerIndex + 1);
                });

        return Flux.concat(statusEvent, tokenStream);
    }
}