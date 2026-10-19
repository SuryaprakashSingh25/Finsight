package com.finsight.backend.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
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

    public int ingest(Path filePath, String company, String documentTitle, Integer year) throws IOException{
        List<Document> pages=pdfDocumentLoader.load(filePath,company,documentTitle,year);
        return ingestPages(pages);
    }

    private int ingestPages(List<Document> pages){
        if(pages.isEmpty()){
            throw new IllegalArgumentException("The PDF contains no extractable text.");
        }

        List<Document> chunks=documentChunker.chunk(pages);

        if(chunks.isEmpty()){
            throw new IllegalArgumentException("No text chunks could be created from the PDF.");
        }

        for(int i=0;i<chunks.size();i++){
            chunks.get(i).getMetadata().put("chunk_index",i);
        }

        for(int start=0;start<chunks.size();start+=BATCH_SIZE){
            int end=Math.min(start+BATCH_SIZE,chunks.size());
            List<Document> batch=chunks.subList(start,end);
            vectorStore.add(batch);
        }
        return chunks.size();
    }
}