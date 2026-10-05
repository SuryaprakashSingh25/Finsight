package com.finsight.backend.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class OpenRouterAIProvider implements AIProvider{

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public OpenRouterAIProvider(
            @Value("${openrouter.base-url}") String baseUrl,
            @Value("${openrouter.api-key}") String apiKey,
            @Value("${openrouter.model}") String model){
                this.apiKey=apiKey;
                this.model=model;

                this.restClient= RestClient.builder()
                        .baseUrl(baseUrl)
                        .build();
    }


    @Override
    public String generate(String prompt) {
        Map<String, Object> requestBody=Map.of(
                "model",model,
                "messages", List.of(
                        Map.of(
                                "role","user",
                                "content",prompt
                        )
                )
        );
        Map<?,?> response=restClient.post()
                .uri("/chat/completions")
                .header("Authorization","Bearer "+apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        (request,resp) -> {
                            int statusCode=resp.getStatusCode().value();

                            boolean retryable=statusCode==429 || statusCode>=500;

                            throw new AIProviderException(
                                    "openrouter",
                                    statusCode,
                                    retryable,
                                    "OpenRouter request failed with status "+statusCode
                            );
                        }
                )
                .body(Map.class);
        return extractResponse(response);
    }

    private String extractResponse(Map<?,?> response){
        List<?> choices=(List<?>) response.get("choices");
        if(choices==null || choices.isEmpty()){
            throw new IllegalStateException("OpenRouter returned no choices");
        }
        Map<?,?> firstChoice=(Map<?, ?>) choices.get(0);
        Map<?,?> message=(Map<?, ?>) firstChoice.get("message");
        if(message==null){
            throw new IllegalStateException("OpenRouter response contains no message");
        }

        Object content=message.get("content");
        if(content==null){
            throw new IllegalStateException("OpenRouter response contains no content");
        }
        return content.toString();
    }

    @Override
    public String getProviderName() {
        return "openrouter";
    }
}
