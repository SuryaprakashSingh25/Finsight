package com.finsight.mcp_server.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

@Component
public class CalculatorTool {

    private static final Logger log =
            LoggerFactory.getLogger(CalculatorTool.class);

    @McpTool(
            name = "percentageGrowth",
            description = "Calculate the percentage growth from a previous value to a current value"
    )
    public double percentageGrowth(
            double current,
            double previous
    ) {
        log.info(
                "MCP TOOL INVOKED: percentageGrowth(current={}, previous={})",
                current,
                previous
        );

        if (previous == 0) {
            throw new IllegalArgumentException(
                    "Previous value cannot be zero"
            );
        }

        return ((current - previous) / previous) * 100;
    }
}