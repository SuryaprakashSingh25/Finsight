package com.finsight.backend.catalog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}