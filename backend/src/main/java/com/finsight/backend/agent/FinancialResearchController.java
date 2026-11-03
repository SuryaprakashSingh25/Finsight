package com.finsight.backend.agent;

import com.finsight.backend.ai.AgentResponse;
import com.finsight.backend.mcp.McpAgentService;
import com.finsight.backend.mcp.StreamEvent;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/agent")
public class FinancialResearchController {

    private final FinancialResearchAgent agent;
    private final McpAgentService mcpAgentService;

    public FinancialResearchController(FinancialResearchAgent agent, McpAgentService mcpAgentService) {
        this.agent = agent;
        this.mcpAgentService = mcpAgentService;
    }

    // Existing blocking endpoint
    @GetMapping
    public AgentResponse ask(@RequestParam String question){
        return agent.ask(question);
    }

    // New reactive SSE streaming endpoint
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<StreamEvent>> streamAsk(@RequestParam String question) {
        return mcpAgentService.streamAsk(question)
                .map(event -> ServerSentEvent.<StreamEvent>builder()
                        .event(event.type())
                        .data(event)
                        .build());
    }
}