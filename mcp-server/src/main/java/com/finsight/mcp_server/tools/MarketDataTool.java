package com.finsight.mcp_server.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MarketDataTool {

    private static final Logger log = LoggerFactory.getLogger(MarketDataTool.class);
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public MarketDataTool(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://query1.finance.yahoo.com")
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) FinSight/1.0")
                .build();
    }

    @McpTool(
            name = "getLiveMarketQuote",
            description = "Fetch real-time stock price, 52-week high, 52-week low, day volume, and currency for a publicly traded company ticker symbol (e.g. NVDA, AAPL, MSFT, RELIANCE.NS)."
    )
    public String getLiveMarketQuote(String symbol) {
        log.info("MCP TOOL: getLiveMarketQuote for ticker '{}'", symbol);

        try {
            String json = restClient.get()
                    .uri("/v8/finance/chart/{symbol}?interval=1d&range=1d", symbol.trim().toUpperCase())
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(json);
            JsonNode result = root.path("chart").path("result");

            if (result.isMissingNode() || !result.isArray() || result.isEmpty()) {
                return "No market data found for ticker: " + symbol;
            }

            JsonNode meta = result.get(0).path("meta");
            double regularMarketPrice = meta.path("regularMarketPrice").asDouble();
            double fiftyTwoWeekHigh = meta.path("fiftyTwoWeekHigh").asDouble();
            double fiftyTwoWeekLow = meta.path("fiftyTwoWeekLow").asDouble();
            long regularMarketVolume = meta.path("regularMarketVolume").asLong();
            String currency = meta.path("currency").asText("USD");
            String exchangeName = meta.path("exchangeName").asText("");

            return """
                    Live Market Quote for %s (%s):
                    - Current Price: %.2f %s
                    - 52-Week Range: %.2f - %.2f %s
                    - Trading Volume: %,d
                    """.formatted(
                    symbol.toUpperCase(),
                    exchangeName,
                    regularMarketPrice,
                    currency,
                    fiftyTwoWeekLow,
                    fiftyTwoWeekHigh,
                    currency,
                    regularMarketVolume
            );
        } catch (Exception e) {
            log.error("Failed to fetch live quote for ticker {}", symbol, e);
            return "Unable to retrieve live market quote for " + symbol + ": " + e.getMessage();
        }
    }
}