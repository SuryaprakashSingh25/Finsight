package com.finsight.backend.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class DocumentIngestionService {

    private static final int BATCH_SIZE = 20;

    private final PdfDocumentLoader pdfDocumentLoader;
    private final DocumentChunker documentChunker;
    private final VectorStore vectorStore;

    public DocumentIngestionService(
            PdfDocumentLoader pdfDocumentLoader,
            DocumentChunker documentChunker,
            VectorStore vectorStore
    ) {
        this.pdfDocumentLoader = pdfDocumentLoader;
        this.documentChunker = documentChunker;
        this.vectorStore = vectorStore;
    }

    public int ingest(String fileName) throws IOException {

        List<Document> pages =
                pdfDocumentLoader.load(fileName);

        List<Document> chunks =
                documentChunker.chunk(pages);

        for (int start = 0; start < chunks.size(); start += BATCH_SIZE) {

            int end = Math.min(
                    start + BATCH_SIZE,
                    chunks.size()
            );

            List<Document> batch =
                    chunks.subList(start, end);

            vectorStore.add(batch);
        }

        return chunks.size();
    }
}