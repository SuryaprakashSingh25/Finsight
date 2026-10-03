package com.finsight.backend.ai;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//@Component
public class VoyageEmbeddingModel implements EmbeddingModel {
    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public VoyageEmbeddingModel(
            @Value("${voyage.api-key}") String apiKey,
            @Value("${voyage.base-url}") String baseUrl,
            @Value("${voyage.model}") String model
    ){
        this.apiKey=apiKey;
        this.model=model;

        this.restClient=RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<String> texts=request.getInstructions();

        Map<String,Object> requestBoy=Map.of(
                "input", texts,
                "model",model
        );
        Map<?,?> response=restClient.post()
                .uri("/embeddings")
                .header("Authorization", "Bearer "+apiKey)
                .body(requestBoy)
                .retrieve()
                .body(Map.class);

        return extractResponse(response);
    }

    @Override
    public float[] embed(Document document) {
        EmbeddingRequest request=new EmbeddingRequest(List.of(document.getText()),null);
        EmbeddingResponse response=call(request);
        return response.getResults().get(0).getOutput();
    }

    private EmbeddingResponse extractResponse(Map<?,?> response){
        List<?> data=(List<?>) response.get("data");
        if(data==null || data.isEmpty()){
            throw new IllegalStateException("Voyage returned no embedding data");
        }

        List<Embedding> embeddings=new ArrayList<>();
        for(Object item:data){
            Map<?,?> dataItem=(Map<?, ?>) item;
            List<?> values=(List<?>) dataItem.get("embedding");
            float[] vector=new float[values.size()];
            for(int i=0;i<values.size();i++){
                vector[i]=((Number) values.get(i)).floatValue();
            }
            int index=((Number) dataItem.get("index")).intValue();

            embeddings.add(new Embedding(vector,index));
        }
        return new EmbeddingResponse(embeddings);
    }
}
