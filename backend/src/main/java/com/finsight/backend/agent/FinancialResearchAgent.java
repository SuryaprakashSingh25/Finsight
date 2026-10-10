package com.finsight.backend.agent;

import com.finsight.backend.ai.AgentResponse;
import com.finsight.backend.mcp.AgentResult;
import com.finsight.backend.mcp.McpAgentService;
import com.finsight.backend.rag.RAGResponse;
import com.finsight.backend.rag.RagContext;
import com.finsight.backend.rag.RagService;
import org.springframework.stereotype.Service;

@Service
public class FinancialResearchAgent {
    private final RagService ragService;
    private final McpAgentService mcpAgentService;

    public FinancialResearchAgent(RagService ragService, McpAgentService mcpAgentService){
        this.ragService=ragService;
        this.mcpAgentService=mcpAgentService;
    }

    public AgentResponse ask(String question){
        RagContext ragContext=ragService.retrieve(question);
        String enrichedQuestion = """
                User question:
                %s

                Relevant financial research from FinSight:
                %s

                Answer the user's question using the available
                research context and tools.
                """.formatted(
                question,
                ragContext.context()
        );
        AgentResult result=mcpAgentService.ask(enrichedQuestion);
        return new AgentResponse(
                result.response(), result.provider()
        );
    }
}
