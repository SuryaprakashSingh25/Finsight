package com.finsight.backend.agent;

import com.finsight.backend.ai.AgentResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class FinancialResearchController {
    private final FinancialResearchAgent agent;

    public FinancialResearchController(FinancialResearchAgent agent){
        this.agent=agent;
    }

    @GetMapping
    public AgentResponse ask(@RequestParam String question){
        return agent.ask(question);
    }
}
