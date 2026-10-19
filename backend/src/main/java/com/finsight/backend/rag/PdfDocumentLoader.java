package com.finsight.backend.rag;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class PdfDocumentLoader {

    public List<Document> load(Path filePath,
                               String company,
                               String documentTitle,
                               Integer year) throws IOException{
        byte[] pdfBytes= Files.readAllBytes(filePath);
        return parsePdf(pdfBytes,company,documentTitle,year);
    }

    private List<Document> parsePdf(
            byte[] pdfBytes,
            String company,
            String documentTitle,
            Integer year
    ) throws  IOException{
        try(PDDocument pdfDocument=Loader.loadPDF(pdfBytes)){
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
                document.getMetadata().put("company",company);
                document.getMetadata().put("document",documentTitle);
                document.getMetadata().put("page",page);
                if(year!=null){
                    document.getMetadata().put("year",year);
                }
                documents.add(document);
            }
            return documents;
        }
    }
}
