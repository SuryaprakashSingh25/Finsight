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
                                You are FinSight, an AI financial research assistant.

                                Use available tools when they are useful.
                                Do not perform arithmetic yourself when a
                                calculator tool is available.
                                Use the provided research context to answer
                                questions about financial documents.
                                If the context does not contain the answer,
                                clearly say so.
                                Return a concise explanation.
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