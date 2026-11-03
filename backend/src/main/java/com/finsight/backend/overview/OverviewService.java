package com.finsight.backend.overview;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OverviewService {

    private final JdbcClient jdbcClient;

    public OverviewService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public OverviewStatsDTO getStats() {
        String companiesSql = "SELECT COUNT(DISTINCT metadata->>'company') FROM vector_store WHERE metadata->>'company' IS NOT NULL";
        Long companiesCount = jdbcClient.sql(companiesSql).query(Long.class).optional().orElse(0L);

        String reportsSql = "SELECT COUNT(DISTINCT CONCAT(metadata->>'company', '::', metadata->>'document')) FROM vector_store WHERE metadata->>'company' IS NOT NULL";
        Long reportsCount = jdbcClient.sql(reportsSql).query(Long.class).optional().orElse(0L);

        String chunksSql = "SELECT COUNT(*) FROM vector_store";
        Long chunksCount = jdbcClient.sql(chunksSql).query(Long.class).optional().orElse(0L);

        String recentSql = """
            SELECT 
                metadata->>'company' AS company,
                metadata->>'document' AS document_title,
                (metadata->>'year')::int AS report_year,
                COUNT(*) AS chunk_count
            FROM vector_store
            WHERE metadata->>'company' IS NOT NULL
            GROUP BY metadata->>'company', metadata->>'document', (metadata->>'year')::int
            LIMIT 5
        """;

        List<Map<String, Object>> rows = jdbcClient.sql(recentSql).query().listOfRows();
        List<OverviewStatsDTO.RecentActivityDTO> recentActivity = rows.stream().map(row -> new OverviewStatsDTO.RecentActivityDTO(
                (String) row.get("company"),
                (String) row.get("document_title"),
                (Integer) row.get("report_year"),
                ((Number) row.get("chunk_count")).intValue()
        )).toList();

        int providersCount = 3;

        return new OverviewStatsDTO(companiesCount, reportsCount, chunksCount, providersCount, recentActivity);
    }
}