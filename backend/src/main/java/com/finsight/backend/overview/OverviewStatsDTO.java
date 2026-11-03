package com.finsight.backend.overview;

import java.util.List;

public record OverviewStatsDTO(
        long companiesTracked,
        long reportsIndexed,
        long totalChunksIndexed,
        int activeAiProviders,
        List<RecentActivityDTO> recentActivity
) {
    public record RecentActivityDTO(
            String company,
            String documentTitle,
            Integer year,
            int chunks
    ) {}
}