package com.finsight.backend.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class McpAgentService {

    private static final Logger log= LoggerFactory.getLogger(McpAgentService.class);

    private final List<AgentProvider> providers;

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

    public AgentResult ask(String question) {
        RuntimeException lastException=null;

        for(AgentProvider provider:providers){
            try{
                log.info("Attemting agent request with provider={}",provider.name());
                String response=provider.chatClient()
                        .prompt()
                        .system("""
                                     You are FinSight, an elite financial research assistant and investment analyst.

                                    Guidelines:
                                    1. Context Priority: Use the provided document context to extract historical GAAP figures (Revenue, Operating Margin, Net Income, CapEx).
                                    2. Strict Math via Tools: NEVER perform arithmetic calculations yourself. You MUST invoke the appropriate MCP calculator tool:
                                       - percentageGrowth for periodic increases/decreases.
                                       - profitMargin for operating or gross margin ratios.
                                       - cagr for multi-year growth trends.
                                       - debtToEquity for leverage assessment.
                                       - freeCashFlow for cash generation.
                                    3. Real-Time News & Market Context: When the user asks about recent stock performance, management sentiment, headwinds, or current events, invoke the 'getRecentCompanyNews' tool.
                                    4. Synthesize: Blend historical 10-K report facts with current news context. If information is missing from both the document and tools, state the limitation clearly.
                                    5. Tone: Objective, quantitative, and concise. Format output using clean Markdown headers, bullet points, and tables.
                                    6. Do NOT output raw LaTeX math delimiters like $$ or \\( \\). Write financial figures and formulas as clean standard text (e.g., "$15.2B", "Revenue growth = 22.4%").
                                    """)
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
}