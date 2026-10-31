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

    @McpTool(
            name = "altmanZScore",
            description = "Calculate the Altman Z-Score for bankruptcy risk assessment for manufacturing/public firms. " +
                    "Formula: 1.2*(workingCapital/totalAssets) + 1.4*(retainedEarnings/totalAssets) + 3.3*(ebit/totalAssets) + 0.6*(marketValueOfEquity/totalLiabilities) + 0.999*(sales/totalAssets). " +
                    "Z > 2.99 = Safe Zone, 1.81 <= Z <= 2.99 = Grey Zone, Z < 1.81 = Distress Zone."
    )
    public double altmanZScore(double workingCapital, double retainedEarnings, double ebit, double marketValueOfEquity, double sales, double totalAssets, double totalLiabilities
    ) {
        log.info("MCP TOOL: altmanZScore invoked for assets={}, liabilities={}", totalAssets, totalLiabilities);
        if (totalAssets <= 0 || totalLiabilities <= 0) {
            throw new IllegalArgumentException("Total Assets and Total Liabilities must be strictly positive non-zero values.");
        }

        double x1 = workingCapital / totalAssets;
        double x2 = retainedEarnings / totalAssets;
        double x3 = ebit / totalAssets;
        double x4 = marketValueOfEquity / totalLiabilities;
        double x5 = sales / totalAssets;

        return (1.2 * x1) + (1.4 * x2) + (3.3 * x3) + (0.6 * x4) + (0.999 * x5);
    }

    @McpTool(
            name = "dupontAnalysis",
            description = "Perform a DuPont 3-Step ROE Decomposition to determine whether returns stem from margins, asset efficiency, or leverage. " +
                    "Returns a structured breakdown: Profit Margin (netIncome/revenue), Asset Turnover (revenue/totalAssets), Equity Multiplier (totalAssets/shareholdersEquity), and resulting ROE."
    )
    public String dupontAnalysis(double netIncome, double revenue, double totalAssets, double shareholdersEquity
    ) {
        log.info("MCP TOOL: dupontAnalysis for revenue={}, equity={}", revenue, shareholdersEquity);
        if (revenue <= 0 || totalAssets <= 0 || shareholdersEquity <= 0) {
            throw new IllegalArgumentException("Revenue, Total Assets, and Equity must be strictly positive non-zero values.");
        }

        double profitMargin = (netIncome / revenue) * 100.0;
        double assetTurnover = revenue / totalAssets;
        double equityMultiplier = totalAssets / shareholdersEquity;
        double roe = (profitMargin / 100.0) * assetTurnover * equityMultiplier * 100.0;

        return """
                DuPont 3-Step ROE Breakdown:
                - Net Profit Margin: %.2f%% (Operating efficiency)
                - Asset Turnover: %.2fx (Asset utilization)
                - Equity Multiplier (Leverage): %.2fx (Financial leverage)
                - Calculated ROE: %.2f%%
                """.formatted(profitMargin, assetTurnover, equityMultiplier, roe);
    }
}