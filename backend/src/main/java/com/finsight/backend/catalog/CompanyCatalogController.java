package com.finsight.backend.catalog;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyCatalogController {

    private final CompanyCatalogService catalogService;

    public CompanyCatalogController(CompanyCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<CompanySummaryDTO> getCompanies() {
        return catalogService.getTrackedCompanies();
    }

    @DeleteMapping("/{company}")
    public ResponseEntity<Void> deleteCompany(@PathVariable String company) {
        catalogService.deleteCompany(company);
        return ResponseEntity.noContent().build();
    }
}