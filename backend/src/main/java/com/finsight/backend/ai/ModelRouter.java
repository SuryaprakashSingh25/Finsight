package com.finsight.backend.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModelRouter {
    private static final Logger log = LoggerFactory.getLogger(ModelRouter.class);
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
            log.info("Trying AI provider: {}",providerName);
            try{
                String response=provider.generate(prompt);
                log.info("AI provider succeeded: {}",providerName);
                return new AIResponse(
                        response,
                        provider.getProviderName()
                );
            } catch (AIProviderException exception){
                log.warn(
                        "AI provider failed: {} status={} retryable={}",
                        exception.getProvider(),
                        exception.getStatusCode(),
                        exception.isRetryable());
                lastException=exception;
                if(!exception.isRetryable()){
                    throw exception;
                }
            } catch (Exception exception){
                log.warn(
                        "Unexpected failure from AI provider: {} - {}",
                        providerName,
                        exception.getMessage()
                );
                lastException=exception;
            }
        }
        throw new IllegalStateException("All AI providers failed", lastException);
    }
}
