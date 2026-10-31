package com.finsight.backend.tearsheet;

import com.finsight.backend.mcp.AgentResult;
import com.finsight.backend.mcp.McpAgentService;
import com.finsight.backend.rag.RagContext;
import com.finsight.backend.rag.RagService;
import org.springframework.stereotype.Service;

@Service
public class TearSheetService {

    private final RagService ragService;
    private final McpAgentService mcpAgentService;

    public TearSheetService(RagService ragService, McpAgentService mcpAgentService) {
        this.ragService = ragService;
        this.mcpAgentService = mcpAgentService;
    }

    public TearSheetResponse generateTearSheet(String company, Integer year, String ticker) {
        String ragQuery = "revenue, operating income, net income, total assets, total liabilities, equity, cash flow, debt";
        RagContext ragContext = ragService.retrieveForCompany(ragQuery, company, year);

        String prompt = """
                OUTPUT DIRECTIVE:
                - Output ONLY the final Executive Tear Sheet formatted in clean Markdown.
                - DO NOT write any chain-of-thought, internal reasoning, draft calculations, or thoughts like "Let's compute", "Let's see", "Wait", etc.
                - Begin your response IMMEDIATELY with the title '# Executive Tear Sheet: %s'.

                TASK:
                Generate a 1-page financial tear sheet for:
                Company: %s
                Fiscal Period: %s
                Ticker: %s

                Retrieved Financial Disclosures:
                %s

                EXECUTION STEPS:
                1. Market Data: Invoke 'getLiveMarketQuote' for ticker '%s' if applicable.
                2. Fundamental Metrics: Invoke MCP tools ('profitMargin', 'debtToEquity', 'dupontAnalysis', 'altmanZScore').
                   - If exact figures for Altman Z-Score or DuPont are not fully present, calculate with the closest GAAP figures available and state the footnote clearly. Do not deliberate in the output.
                3. Market Context: Invoke 'getRecentCompanyNews' for %s.
                4. Output Structure:
                   # Executive Tear Sheet: %s (%s)
                   ## 1. Key Fundamentals & Valuation Matrix
                   (Markdown table: Metric | Value | Reference / Status)
                   ## 2. Capital Structure & Solvency Health
                   (Altman Z-Score analysis & Debt-to-Equity evaluation)
                   ## 3. DuPont Efficiency Decomposition
                   (Analysis of margin vs. asset turnover vs. leverage)
                   ## 4. Market Sentiment & Headwinds
                   (Bullet points from live news)
                   ## 5. Strategic Analyst Verdict
                """.formatted(
                company,
                company,
                year != null ? year : "Latest Available",
                ticker != null && !ticker.isBlank() ? ticker : company,
                ragContext.context(),
                ticker != null && !ticker.isBlank() ? ticker : company,
                company,
                company,
                year != null ? year : "FY"
        );

        AgentResult result = mcpAgentService.ask(prompt);

        // Clean up any stray internal thoughts if the model still emits leading text
        String cleanedResponse = stripScratchpad(result.response());

        return new TearSheetResponse(company, year, cleanedResponse, result.provider());
    }

    /**
     * Defense-in-depth: If the LLM produces thinking text before the title,
     * strip everything prior to the first Markdown header '# '.
     */
    private String stripScratchpad(String response) {
        if (response == null) return "";
        int headerIndex = response.indexOf("# ");
        if (headerIndex > 0) {
            return response.substring(headerIndex).trim();
        }
        return response.trim();
    }
}