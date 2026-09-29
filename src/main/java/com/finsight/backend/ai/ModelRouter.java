package com.finsight.backend.ai;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModelRouter {
    private final List<AIProvider> providers;

    public ModelRouter(List<AIProvider> providers){
        this.providers=providers;
    }

    public AIProvider getProvider(String providerName){
        return providers.stream()
                .filter(provider -> provider.getProviderName().equalsIgnoreCase(providerName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("AI provider not available: "+providerName));
    }

    public AIResponse generateWithFallback(String prompt){
        List<String> providerOrder=List.of(
                "openrouter",
                "groq",
                "gemini"
        );
        Exception lastException=null;
        for(String providerName: providerOrder){
            AIProvider provider=getProvider(providerName);
            try{
                String response=provider.generate(prompt);
                return new AIResponse(
                        response,
                        provider.getProviderName()
                );
            } catch (Exception exception){
                lastException=exception;
            }
        }
        throw new IllegalStateException("All AI providers failed", lastException);
    }
}
