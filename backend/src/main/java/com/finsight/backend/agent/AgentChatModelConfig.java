package com.finsight.backend.agent;

import com.google.genai.Client;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentChatModelConfig {

    @Bean
    public ChatModel groqAgentChatModel(
            @Value("${groq.agent-base-url}") String baseUrl,
            @Value("${groq.api-key}") String apiKey,
            @Value("${groq.model}") String model
    ){
        OpenAiApi groqApi= OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();

        OpenAiChatOptions options= OpenAiChatOptions.builder()
                .model(model)
                .build();

        return OpenAiChatModel
                .builder()
                .openAiApi(groqApi)
                .defaultOptions(options)
                .build();
    }

    @Bean
    public ChatModel geminiAgentChatModel(
            @Value("${spring.ai.google.genai.api-key}") String apiKey,
            @Value("${spring.ai.google.genai.chat.options.model:gemini-3.8-flash}")
            String model
    ){
        Client genAiClient= Client.builder()
                .apiKey(apiKey)
                .build();

        GoogleGenAiChatOptions options= GoogleGenAiChatOptions.builder()
                .model(model)
                .build();

        return GoogleGenAiChatModel.builder()
                .genAiClient(genAiClient)
                .defaultOptions(options)
                .build();
    }
}
