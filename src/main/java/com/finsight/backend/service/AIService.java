package com.finsight.backend.service;

import com.finsight.backend.ai.AIProvider;
import org.springframework.stereotype.Service;

@Service
public class AIService {
    private final AIProvider aiProvider;

    public AIService(AIProvider aiProvider){
        this.aiProvider=aiProvider;
    }

    public String generate(String prompt){
        return aiProvider.generate(prompt);
    }

    public String getProviderName(){
        return aiProvider.getProviderName();
    }
}
