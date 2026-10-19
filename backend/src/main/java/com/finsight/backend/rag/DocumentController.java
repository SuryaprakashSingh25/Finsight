package com.finsight.backend.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private static final Logger log= LoggerFactory.getLogger(DocumentController.class);

    private static final long MAX_FILE_SIZE=20*1024*1024;
    private final DocumentIngestionService ingestionService;

    public DocumentController(DocumentIngestionService ingestionService){
        this.ingestionService=ingestionService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String,Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("company") String company,
            @RequestParam("documentTitle") String documentTitle,
            @RequestParam(value="year", required = false) Integer year
            ){
        if(file.isEmpty()){
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Please select a PDF file."
            ));
        }

        if(file.getSize()>MAX_FILE_SIZE){
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "PDF files must be 20 MB or smaller."
            ));
        }

        if(company.isBlank() || documentTitle.isBlank()){
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Company and document title are required."
            ));
        }

        String originalName=file.getOriginalFilename();
        if(originalName==null || !originalName.toLowerCase(Locale.ROOT).endsWith(".pdf")){
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Only PDF files are supported."
            ));
        }

        Path uploadDirectory= Paths.get("data","uploads")
                .toAbsolutePath()
                .normalize();

        Path savedFile=null;
        try{
            Files.createDirectories(uploadDirectory);
            savedFile=uploadDirectory.resolve(UUID.randomUUID()+".pdf")
                    .normalize();

            if(!savedFile.startsWith(uploadDirectory)){
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Invalid upload path."
                ));
            }

            file.transferTo(savedFile);

            int chunks= ingestionService.ingest(savedFile,company.trim(),documentTitle.trim(),year);

            log.info("Ingested document '{}' for company '{}': {} chunks",documentTitle,company,chunks);

            return ResponseEntity.ok(Map.of(
                    "status","SUCCESS",
                    "company",company.trim(),
                    "documentTitle",documentTitle.trim(),
                    "chunksIndexed", chunks
            ));
        } catch (IOException | RuntimeException exception){
            log.error("Failed to ingest uploaded document",exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Document ingestion failed. Check backend logs."
            ));
        } finally {
            if (savedFile != null) {
                try {
                    Files.deleteIfExists(savedFile);
                } catch (IOException e) {
                    log.warn("Could not delete temp upload file: {}", savedFile, e);
                }
            }
        }
    }
}
