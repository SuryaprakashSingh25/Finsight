package com.finsight.backend.mcp;

import org.springframework.ai.chat.client.ChatClient;

public record AgentProvider(
        String name,
        ChatClient chatClient
) {
}
