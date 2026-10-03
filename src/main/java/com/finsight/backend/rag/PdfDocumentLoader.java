package com.finsight.backend.rag;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class PdfDocumentLoader {
    public List<Document> load(String fileName) throws IOException {
        ClassPathResource resource=new ClassPathResource("documents/"+fileName);
        try(InputStream inputStream=resource.getInputStream()){
            byte[] pdfBytes= inputStream.readAllBytes();
            try (PDDocument pdfDocument= Loader.loadPDF(pdfBytes)){
                PDFTextStripper stripper=new PDFTextStripper();
                List<Document> documents=new ArrayList<>();

                for(int page=1;page<=pdfDocument.getNumberOfPages();page++){
                    stripper.setStartPage(page);
                    stripper.setEndPage(page);

                    String text=stripper.getText(pdfDocument).trim();
                    if(text.isBlank()){
                        continue;
                    }
                    Document document=new Document(text);
                    document.getMetadata().put(
                            "company",
                            "NVIDIA"
                    );

                    document.getMetadata().put(
                            "document",
                            "2026 Annual Report"
                    );

                    document.getMetadata().put(
                            "year",
                            2026
                    );

                    document.getMetadata().put(
                            "page",
                            page
                    );

                    documents.add(document);
                }
                return documents;

            } catch (IOException exception){
                throw new IllegalStateException(
                        "Failed to load PDF: "+fileName,exception
                );
            }
        }
    }
}
