package com.finsight.backend.comparison;

import com.finsight.backend.mcp.AgentResult;
import com.finsight.backend.mcp.McpAgentService;
import com.finsight.backend.rag.RagContext;
import com.finsight.backend.rag.RagService;
import org.springframework.stereotype.Service;

@Service
public class ComparisonService {

    private final RagService ragService;
    private final McpAgentService mcpAgentService;

    public ComparisonService(RagService ragService, McpAgentService mcpAgentService) {
        this.ragService = ragService;
        this.mcpAgentService = mcpAgentService;
    }

    public ComparisonResponse compare(ComparisonRequest req) {
        String query = req.metricFocus() != null && !req.metricFocus().isBlank()
                ? req.metricFocus()
                : "revenue, operating income, and profit margins";

        RagContext contextA = ragService.retrieveForCompany(query, req.companyA(), req.yearA());
        RagContext contextB = ragService.retrieveForCompany(query, req.companyB(), req.yearB());

        String prompt = """
                You are FinSight's Senior Comparative Financial Analyst.
                Perform a structured head-to-head financial comparison between:
                
                Entity A: %s (Year: %s)
                Entity B: %s (Year: %s)
                
                Focus Area: %s
                
                Document Context for Entity A:
                %s
                
                Document Context for Entity B:
                %s
                
                Instructions:
                1. Identify the matching financial metrics for both entities from the context.
                2. Use the available MCP calculator tools (percentageGrowth, operatingMargin, cagr) to compute relative variances and differences accurately.
                3. Structure your response with:
                   - Executive Summary Table (comparing metrics side-by-side)
                   - Strategic Variance Commentary (outlining growth or efficiency advantages)
                   - Evidence & Data Limitations
                """.formatted(
                req.companyA(), req.yearA() != null ? req.yearA() : "Latest",
                req.companyB(), req.yearB() != null ? req.yearB() : "Latest",
                query,
                contextA.context(),
                contextB.context()
        );

        AgentResult result = mcpAgentService.ask(prompt);
        return new ComparisonResponse(result.response(), result.provider());
    }
}
