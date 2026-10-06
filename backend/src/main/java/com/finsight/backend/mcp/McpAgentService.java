package com.finsight.backend.mcp;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

@Service
public class McpAgentService {

    private final ChatClient chatClient;

    public McpAgentService(
            ChatClient.Builder chatClientBuilder,
            ToolCallbackProvider mcpTools
    ) {
        this.chatClient = chatClientBuilder
                .defaultToolCallbacks(mcpTools)
                .build();
    }

    public String ask(String question) {

        return chatClient
                .prompt()
                .user(question)
                .call()
                .content();
    }
}