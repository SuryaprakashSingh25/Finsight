package com.finsight.mcp_server.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

@Component
public class CalculatorTool {

    private static final Logger log = LoggerFactory.getLogger(CalculatorTool.class);

    @McpTool(
            name = "percentageGrowth",
            description = "Calculate the percentage growth from a previous value to a current value"
    )
    public double percentageGrowth(double current, double previous) {
        log.info("MCP TOOL: percentageGrowth(current={}, previous={})", current, previous);
        if (previous == 0) {
            throw new IllegalArgumentException("Previous value cannot be zero");
        }
        return ((current - previous) / previous) * 100.0;
    }

    @McpTool(
            name = "operatingMargin",
            description = "Calculate the operating profit margin percentage given operating income and revenue"
    )
    public double operatingMargin(double operatingIncome, double totalRevenue) {
        log.info("MCP TOOL: operatingMargin(operatingIncome={}, totalRevenue={})", operatingIncome, totalRevenue);
        if (totalRevenue == 0) {
            throw new IllegalArgumentException("Total revenue cannot be zero");
        }
        return (operatingIncome / totalRevenue) * 100.0;
    }

    @McpTool(
            name = "cagr",
            description = "Calculate Compound Annual Growth Rate (CAGR) as a percentage over a given number of periods/years"
    )
    public double cagr(double beginningValue, double endingValue, double periods) {
        log.info("MCP TOOL: cagr(beg={}, end={}, periods={})", beginningValue, endingValue, periods);
        if (beginningValue <= 0 || endingValue <= 0 || periods <= 0) {
            throw new IllegalArgumentException("Values and periods must be positive and non-zero");
        }
        return (Math.pow(endingValue / beginningValue, 1.0 / periods) - 1.0) * 100.0;
    }

    @McpTool(
            name = "debtToEquity",
            description = "Calculate the Debt-to-Equity (D/E) ratio"
    )
    public double debtToEquity(double totalDebt, double totalEquity) {
        log.info("MCP TOOL: debtToEquity(totalDebt={}, totalEquity={})", totalDebt, totalEquity);
        if (totalEquity == 0) {
            throw new IllegalArgumentException("Total equity cannot be zero");
        }
        return totalDebt / totalEquity;
    }
}