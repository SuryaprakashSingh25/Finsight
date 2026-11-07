package com.finsight.backend.tearsheet;

import com.finsight.backend.auth.SecurityUtils;
import com.finsight.backend.mcp.AgentResult;
import com.finsight.backend.mcp.McpAgentService;
import com.finsight.backend.rag.RagContext;
import com.finsight.backend.rag.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TearSheetService {

    private static final Logger log = LoggerFactory.getLogger(TearSheetService.class);

    private final RagService ragService;
    private final McpAgentService mcpAgentService;
    private final SecurityUtils securityUtils;
    private final TearSheetCacheService cacheService;

    public TearSheetService(
            RagService ragService,
            McpAgentService mcpAgentService,
            SecurityUtils securityUtils,
            TearSheetCacheService cacheService
    ) {
        this.ragService = ragService;
        this.mcpAgentService = mcpAgentService;
        this.securityUtils = securityUtils;
        this.cacheService = cacheService;
    }

    public TearSheetResponse generateTearSheet(String company, Integer year, String ticker) {
        String userId = securityUtils.getCurrentUserIdString();

        Optional<TearSheetResponse> cached = cacheService.get(userId, company, year);
        if (cached.isPresent()) {
            return cached.get();
        }

        log.info("Executing live tear sheet generation for user={}, company={}, year={}", userId, company, year);

        String ragQuery = "Consolidated Statements of Operations Balance Sheet total revenue net income assets liabilities debt";
        RagContext ragContext = ragService.retrieveForCompany(ragQuery, company, year, 3);

        String effectiveTicker = (ticker != null && !ticker.isBlank()) ? ticker : company;

        String prompt = """
                OUTPUT DIRECTIVE:
                - Output ONLY the final Executive Tear Sheet in clean Markdown.
                - Do NOT include internal scratchpad or conversational preamble.
                - Begin immediately with '# Executive Tear Sheet: %s'.

                COMPANY: %s (%s)
                TICKER: %s

                FINANCIAL DISCLOSURES FROM FILING:
                %s

                INSTRUCTIONS:
                1. Identify key figures (Revenue, Net Income, Total Assets, Total Liabilities, Total Debt) from the disclosures above.
                2. In your very first turn, invoke tools in parallel:
                   - 'getLiveMarketQuote' for ticker '%s'
                   - 'dupontAnalysis' or 'altmanZScore'
                3. Immediately synthesize the outputs into this structure:
                   # Executive Tear Sheet: %s (%s)
                   ## 1. Key Fundamentals & Valuation Matrix
                   (Table: Metric | Value | Reference / Status)
                   ## 2. Capital Structure & Solvency Health
                   (Altman Z-Score & leverage health evaluation)
                   ## 3. DuPont Efficiency Breakdown
                   ## 4. Strategic Analyst Verdict
                """.formatted(
                company,
                company,
                year != null ? year : "Latest",
                effectiveTicker,
                ragContext.context(),
                effectiveTicker,
                company,
                year != null ? year : "FY"
        );

        AgentResult result = mcpAgentService.ask(prompt);
        String cleanedResponse = stripScratchpad(result.response());

        TearSheetResponse response = new TearSheetResponse(company, year, cleanedResponse, result.provider());

        // 3. Populate Redis Cache
        cacheService.put(userId, company, year, response);

        return response;
    }

    private String stripScratchpad(String response) {
        if (response == null) return "";
        int headerIndex = response.indexOf("# ");
        if (headerIndex >= 0) {
            return response.substring(headerIndex).trim();
        }
        return response.trim();
    }
}