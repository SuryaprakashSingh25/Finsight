package com.finsight.backend.catalog;

import com.finsight.backend.auth.SecurityUtils;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CompanyCatalogService {

    private final JdbcClient jdbcClient;
    private final SecurityUtils securityUtils;

    public CompanyCatalogService(JdbcClient jdbcClient, SecurityUtils securityUtils) {
        this.jdbcClient = jdbcClient;
        this.securityUtils = securityUtils;
    }

    public List<CompanySummaryDTO> getTrackedCompanies() {
        String userId = securityUtils.getCurrentUserIdString();

        // Scope to the authenticated user's uploaded documents
        String sql = """
            SELECT 
                metadata->>'company' AS company,
                metadata->>'document' AS document_title,
                (metadata->>'year')::int AS report_year,
                COUNT(*) AS chunk_count
            FROM vector_store
            WHERE metadata->>'uploaded_by' = :userId
              AND metadata->>'company' IS NOT NULL
            GROUP BY 
                metadata->>'company',
                metadata->>'document',
                (metadata->>'year')::int
            ORDER BY company ASC, report_year DESC NULLS LAST
        """;

        List<Map<String,Object>> rows = jdbcClient.sql(sql)
                .param("userId", userId)
                .query()
                .listOfRows();

        Map<String, List<CompanySummaryDTO.DocumentSummaryDTO>> companyMap = new LinkedHashMap<>();
        Map<String, Integer> chunkTotals = new HashMap<>();

        for (Map<String, Object> row : rows) {
            String company = (String) row.get("company");
            String docTitle = (String) row.get("document_title");
            Integer year = (Integer) row.get("report_year");
            int chunks = ((Number) row.get("chunk_count")).intValue();

            companyMap.computeIfAbsent(company, k -> new ArrayList<>())
                    .add(new CompanySummaryDTO.DocumentSummaryDTO(docTitle, year, chunks));

            chunkTotals.merge(company, chunks, Integer::sum);
        }

        List<CompanySummaryDTO> summaries = new ArrayList<>();
        for (Map.Entry<String, List<CompanySummaryDTO.DocumentSummaryDTO>> entry : companyMap.entrySet()) {
            String comp = entry.getKey();
            List<CompanySummaryDTO.DocumentSummaryDTO> docs = entry.getValue();
            summaries.add(new CompanySummaryDTO(
                    comp,
                    docs.size(),
                    chunkTotals.getOrDefault(comp, 0),
                    docs
            ));
        }

        return summaries;
    }

    public int deleteCompany(String company) {
        String userId = securityUtils.getCurrentUserIdString();

        // Ensure a user can only delete their own uploaded company data
        String sql = """
            DELETE FROM vector_store
            WHERE metadata->>'uploaded_by' = :userId
              AND metadata->>'company' = :company
        """;

        return jdbcClient.sql(sql)
                .param("userId", userId)
                .param("company", company)
                .update();
    }
}