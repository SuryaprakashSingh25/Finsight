package com.finsight.backend.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder){
        this.chatClient=chatClientBuilder.build();
    }

    @PostMapping
    public Map<String,String> chat(@RequestBody Map<String,String> request){
        String message=request.get("message");
        String response=chatClient
                .prompt()
                .user(message)
                .call()
                .content();

        return Map.of(
                "response", response
        );
    }
}
