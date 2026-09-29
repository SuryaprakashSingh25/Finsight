package com.finsight.backend.ai;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModelRouter {
    private final List<AIProvider> providers;

    public ModelRouter(List<AIProvider> providers){
        this.providers=providers;
    }

    public AIProvider getDefaultProvider(){
        return providers.stream()
                .filter(provider -> provider.getProviderName().equals("gemini"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Gemini provider not available"));
    }
}
