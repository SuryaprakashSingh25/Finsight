package com.finsight.backend.mcp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mcp-agent-test")
public class McpAgentTestController {

    private final McpAgentService mcpAgentService;

    public McpAgentTestController(
            McpAgentService mcpAgentService
    ) {
        this.mcpAgentService = mcpAgentService;
    }

    @GetMapping
    public String ask(@RequestParam String question) {
        return mcpAgentService.ask(question);
    }
}