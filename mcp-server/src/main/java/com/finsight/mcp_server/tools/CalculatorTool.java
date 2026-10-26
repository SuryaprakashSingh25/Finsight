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
            description = "Calculate percentage change from previous to current value. Formula: ((current - previous) / previous) * 100"
    )
    public double percentageGrowth(double current, double previous) {
        log.info("MCP TOOL: percentageGrowth(current={}, previous={})", current, previous);
        if (previous == 0) {
            throw new IllegalArgumentException("Previous value cannot be zero");
        }
        return ((current - previous) / previous) * 100.0;
    }

    @McpTool(
            name = "profitMargin",
            description = "Calculate profit margin percentage. Formula: (income / totalRevenue) * 100"
    )
    public double profitMargin(double income, double totalRevenue) {
        log.info("MCP TOOL: profitMargin(income={}, revenue={})", income, totalRevenue);
        if (totalRevenue == 0) {
            throw new IllegalArgumentException("Total revenue cannot be zero");
        }
        return (income / totalRevenue) * 100.0;
    }

    @McpTool(
            name = "cagr",
            description = "Calculate Compound Annual Growth Rate (CAGR) percentage across periods. Formula: ((ending / beginning) ^ (1 / periods) - 1) * 100"
    )
    public double cagr(double beginningValue, double endingValue, double periods) {
        log.info("MCP TOOL: cagr(beg={}, end={}, periods={})", beginningValue, endingValue, periods);
        if (beginningValue <= 0 || endingValue <= 0 || periods <= 0) {
            throw new IllegalArgumentException("Values and periods must be positive non-zero numbers");
        }
        return (Math.pow(endingValue / beginningValue, 1.0 / periods) - 1.0) * 100.0;
    }

    @McpTool(
            name = "debtToEquity",
            description = "Calculate Debt-to-Equity ratio. Formula: totalLiabilities / totalStockholdersEquity"
    )
    public double debtToEquity(double totalLiabilities, double totalStockholdersEquity) {
        log.info("MCP TOOL: debtToEquity(debt={}, equity={})", totalLiabilities, totalStockholdersEquity);
        if (totalStockholdersEquity == 0) {
            throw new IllegalArgumentException("Shareholder equity cannot be zero");
        }
        return totalLiabilities / totalStockholdersEquity;
    }

    @McpTool(
            name = "freeCashFlow",
            description = "Calculate Free Cash Flow. Formula: operatingCashFlow - capitalExpenditures"
    )
    public double freeCashFlow(double operatingCashFlow, double capitalExpenditures) {
        log.info("MCP TOOL: freeCashFlow(ocf={}, capex={})", operatingCashFlow, capitalExpenditures);
        return operatingCashFlow - capitalExpenditures;
    }
}