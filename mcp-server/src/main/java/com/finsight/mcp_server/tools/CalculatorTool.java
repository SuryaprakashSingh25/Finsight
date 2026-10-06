package com.finsight.mcp_server.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class CalculatorTool {

    @Tool(description = "Calculate the percentage growth from a previous value to a current value")
    public double percentageGrowth(
            double current,
            double previous
    ){
        if(previous==0){
            throw new IllegalArgumentException("Previous value cannot be zero");
        }
        return ((current-previous)/previous)*100;
    }
}
