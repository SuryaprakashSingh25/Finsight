package com.finsight.backend.rag;

import com.finsight.backend.ai.AIResponse;
import com.finsight.backend.ai.ModelRouter;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
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

    public AIResponse answer(String question) {

        List<Document> documents = vectorStore.similaritySearch(question);

        if (documents == null || documents.isEmpty()) {
            return new AIResponse(
                    "I could not find relevant information in the available documents.",
                    "none"
            );
        }

        String context = documents.stream()
                .limit(5)
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

        return modelRouter.generateWithFallback(prompt);
    }
}