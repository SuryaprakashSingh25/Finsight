package com.finsight.backend.controller;

import com.finsight.backend.service.AIService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final AIService aiService;

    public ChatController(AIService aiService){
        this.aiService=aiService;
    }

    @PostMapping
    public Map<String,String> chat(@RequestBody Map<String,String> request){
        String message=request.get("message");
        String response=aiService.generate(message);

        return Map.of(
                "response", response,
                "provider", aiService.getProviderName()
        );
    }
}
