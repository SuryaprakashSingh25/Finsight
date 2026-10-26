package com.finsight.mcp_server.tools;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FinancialNewsTool {

    private static final Logger log = LoggerFactory.getLogger(FinancialNewsTool.class);

    @McpTool(
            name = "getRecentCompanyNews",
            description = "Fetch the latest real-time financial news, earnings headlines, and market commentary for a company or stock ticker without any API key."
    )
    public String getRecentCompanyNews(String companyOrTicker) {
        log.info("MCP TOOL: getRecentCompanyNews for '{}'", companyOrTicker);

        try {
            // Encode query for Google News Financial RSS
            String query = URLEncoder.encode(companyOrTicker + " stock financial earnings", StandardCharsets.UTF_8);
            String feedUrl = "https://news.google.com/rss/search?q=" + query + "&hl=en-US&gl=US&ceid=US:en";

            URI uri = URI.create(feedUrl);
            SyndFeedInput input = new SyndFeedInput();

            try (XmlReader reader = new XmlReader(uri.toURL())) {
                SyndFeed feed = input.build(reader);
                List<SyndEntry> entries = feed.getEntries();

                if (entries == null || entries.isEmpty()) {
                    return "No recent financial news articles found for: " + companyOrTicker;
                }

                return entries.stream()
                        .limit(5)
                        .map(entry -> """
                                - Headline: %s
                                  Published: %s
                                  Source: %s
                                  Link: %s
                                """.formatted(
                                entry.getTitle(),
                                entry.getPublishedDate() != null ? entry.getPublishedDate().toString() : "Recent",
                                entry.getSource() != null ? entry.getSource().getTitle() : "Financial News",
                                entry.getLink()
                        ))
                        .collect(Collectors.joining("\n"));
            }
        } catch (Exception ex) {
            log.error("Failed to fetch RSS news for {}", companyOrTicker, ex);
            return "Unable to retrieve live news at this time: " + ex.getMessage();
        }
    }
}