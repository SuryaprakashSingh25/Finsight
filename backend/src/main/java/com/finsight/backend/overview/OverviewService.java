package com.finsight.backend.overview;

import com.finsight.backend.auth.SecurityUtils;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OverviewService {

    private final JdbcClient jdbcClient;
    private final SecurityUtils securityUtils;

    public OverviewService(JdbcClient jdbcClient, SecurityUtils securityUtils) {
        this.jdbcClient = jdbcClient;
        this.securityUtils = securityUtils;
    }

    public OverviewStatsDTO getStats() {
        String userId = securityUtils.getCurrentUserIdString();

        String companiesSql = """
            SELECT COUNT(DISTINCT metadata->>'company') 
            FROM vector_store 
            WHERE metadata->>'uploaded_by' = :userId 
              AND metadata->>'company' IS NOT NULL
        """;
        Long companiesCount = jdbcClient.sql(companiesSql)
                .param("userId", userId)
                .query(Long.class)
                .optional()
                .orElse(0L);

        String reportsSql = """
            SELECT COUNT(DISTINCT CONCAT(metadata->>'company', '::', metadata->>'document')) 
            FROM vector_store 
            WHERE metadata->>'uploaded_by' = :userId 
              AND metadata->>'company' IS NOT NULL
        """;
        Long reportsCount = jdbcClient.sql(reportsSql)
                .param("userId", userId)
                .query(Long.class)
                .optional()
                .orElse(0L);

        String chunksSql = """
            SELECT COUNT(*) 
            FROM vector_store 
            WHERE metadata->>'uploaded_by' = :userId
        """;
        Long chunksCount = jdbcClient.sql(chunksSql)
                .param("userId", userId)
                .query(Long.class)
                .optional()
                .orElse(0L);

        String recentSql = """
            SELECT 
                metadata->>'company' AS company,
                metadata->>'document' AS document_title,
                (metadata->>'year')::int AS report_year,
                COUNT(*) AS chunk_count
            FROM vector_store
            WHERE metadata->>'uploaded_by' = :userId
              AND metadata->>'company' IS NOT NULL
            GROUP BY metadata->>'company', metadata->>'document', (metadata->>'year')::int
            LIMIT 5
        """;

        List<Map<String,Object>> rows = jdbcClient.sql(recentSql)
                .param("userId", userId)
                .query()
                .listOfRows();

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