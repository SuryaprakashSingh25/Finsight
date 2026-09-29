package com.finsight.backend.service;

import com.finsight.backend.ai.AIProvider;
import com.finsight.backend.ai.AIResponse;
import com.finsight.backend.ai.ModelRouter;
import org.springframework.stereotype.Service;

@Service
public class AIService {
    private final ModelRouter modelRouter;

    public AIService(ModelRouter modelRouter){
        this.modelRouter=modelRouter;
    }

    public AIResponse generate(String prompt){

        return modelRouter.generateWithFallback(prompt);
    }
}
