package com.finsight.backend.service;

import com.finsight.backend.ai.AIProvider;
import com.finsight.backend.ai.ModelRouter;
import org.springframework.stereotype.Service;

@Service
public class AIService {
    private final ModelRouter modelRouter;

    public AIService(ModelRouter modelRouter){
        this.modelRouter=modelRouter;
    }

    public String generate(String prompt){

        AIProvider provider=modelRouter.getDefaultProvider();
        return provider.generate(prompt);
    }

    public String getProviderName(){
        AIProvider provider= modelRouter.getDefaultProvider();
        return provider.getProviderName();
    }
}
