package com.finsight.backend.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ModelRouter {
    private static final Logger log = LoggerFactory.getLogger(ModelRouter.class);

    private final Map<String, ChatModel> providers;

    public ModelRouter(
            @Qualifier("openAiChatModel") ChatModel openRouterModel,
            @Qualifier("groqAgentChatModel") ChatModel groqModel,
            @Qualifier("geminiAgentChatModel") ChatModel geminiModel
    ) {
        this.providers = Map.of(
                "openrouter", openRouterModel,
                "groq", groqModel,
                "gemini", geminiModel
        );
    }

    public AIResponse generateWithFallback(String prompt) {
        List<String> providerOrder = List.of("openrouter", "groq", "gemini");
        Exception lastException = null;

        for (String providerName : providerOrder) {
            ChatModel model = providers.get(providerName);
            log.info("Trying AI provider: {}", providerName);
            try {
                String response = model.call(prompt);
                log.info("AI provider succeeded: {}", providerName);
                return new AIResponse(response, providerName);
            } catch (Exception ex) {
                log.warn("AI provider {} failed: {}", providerName, ex.getMessage());
                lastException = ex;
            }
        }
        throw new IllegalStateException("All AI providers failed", lastException);
    }
}