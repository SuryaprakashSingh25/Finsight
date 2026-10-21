package com.finsight.backend.catalog;

import java.util.List;

public record CompanySummaryDTO(
        String company,
        int totalReports,
        int totalChunks,
        List<DocumentSummaryDTO> reports
) {
    public record DocumentSummaryDTO(
            String documentTitle,
            Integer year,
            int chunkCount
    ) {}
}