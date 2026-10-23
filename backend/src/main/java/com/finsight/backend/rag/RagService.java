package com.finsight.backend.rag;

import com.finsight.backend.ai.AIResponse;
import com.finsight.backend.ai.ModelRouter;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final VectorStore vectorStore;
    private final ModelRouter modelRouter;

    public RagService(VectorStore vectorStore, ModelRouter modelRouter) {
        this.vectorStore = vectorStore;
        this.modelRouter = modelRouter;
    }

    public RAGResponse answer(String question) {

        List<Document> documents = vectorStore.similaritySearch(question);

        if (documents == null || documents.isEmpty()) {
            return new RAGResponse(
                    "I could not find relevant information in the available documents.",
                    null,
                    List.of()
            );
        }

        List<Document> relevantDocuments=documents.stream()
                .limit(5)
                .toList();

        String context = relevantDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        String prompt = """
                You are a financial research assistant.

                Answer the user's question using ONLY the provided context.

                If the context does not contain enough information to answer
                the question, say that the available document does not contain
                enough information.

                Do not invent facts, numbers, or sources.

                Context:
                %s

                User question:
                %s
                """.formatted(context, question);

        AIResponse aiResponse=modelRouter.generateWithFallback(prompt);

        List<SourceReference> sources=relevantDocuments.stream()
                .map(document -> new SourceReference(
                        (String) document.getMetadata().get("document"),
                        getIntegerMetadata(document,"page"),
                        getIntegerMetadata(document,"chunk_index")
                ))
                .toList();

        return new RAGResponse(
                aiResponse.response(),
                aiResponse.provider(),
                sources
        );
    }

    private Integer getIntegerMetadata(Document document, String key){
        Object value=document.getMetadata().get(key);
        if(value instanceof Number number){
            return number.intValue();
        }
        return null;
    }

    public RagContext retrieve(String question){
        List<Document> documents=vectorStore.similaritySearch(question);
        if(documents==null || documents.isEmpty()){
            return new RagContext(
                    "",
                    List.of()
            );
        }

        List<Document> relevantDocuments=documents.stream()
                .limit(5)
                .toList();

        String context=relevantDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        List<SourceReference> sources=relevantDocuments.stream()
                .map(document -> new SourceReference(
                        (String) document.getMetadata().get("document"),
                        getIntegerMetadata(document,"page"),
                        getIntegerMetadata(document,"chunk_index")
                )).toList();

        return new RagContext(
                context,
                sources
        );
    }

    public RagContext retrieveForCompany(String question, String company, Integer year){
        FilterExpressionBuilder b=new FilterExpressionBuilder();
        var filterExpression=year!=null?b.and(b.eq("company",company), b.eq("year",year)).build(): b.eq("company",company).build();

        SearchRequest request= SearchRequest.builder()
                .query(question)
                .topK(4)
                .filterExpression(filterExpression)
                .build();

        List<Document> documents=vectorStore.similaritySearch(request);
        if(documents==null || documents.isEmpty()){
            return new RagContext("", List.of());
        }

        String context=documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        List<SourceReference> sources=documents.stream()
                .map(document -> new SourceReference(
                        (String) document.getMetadata().get("document"),
                        getIntegerMetadata(document,"page"),
                        getIntegerMetadata(document,"chunk_index")
                )).toList();

        return new RagContext(context,sources);
    }
}