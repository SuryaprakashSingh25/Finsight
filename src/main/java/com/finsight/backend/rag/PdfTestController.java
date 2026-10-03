package com.finsight.backend.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/pdf-test")
public class PdfTestController {

    private final DocumentIngestionService ingestionService;
    private final VectorStore vectorStore;

    public PdfTestController(
            DocumentIngestionService ingestionService, VectorStore vectorStore
    ) {
        this.ingestionService = ingestionService;
        this.vectorStore=vectorStore;
    }

    @GetMapping("/search")
    public List<Document> search(@RequestParam String query){
        return vectorStore.similaritySearch(query);
    }

    @GetMapping("/ingest")
    public String ingest() throws IOException {

        int chunks = ingestionService.ingest(
                "nvidia-2026-annual-report.pdf"
        );

        return "Successfully ingested " + chunks + " chunks";
    }
}