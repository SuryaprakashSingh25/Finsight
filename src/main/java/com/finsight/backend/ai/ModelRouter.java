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
        AIProvider primary=getProvider("gemini");
        try{
            return new AIResponse(
                    primary.generate(prompt),
                    primary.getProviderName()
            );
        }
        catch (Exception primaryException){
            AIProvider fallback=getProvider("openrouter");
            return new AIResponse(
                    fallback.generate(prompt),
                    fallback.getProviderName()
            );
        }
    }
}
