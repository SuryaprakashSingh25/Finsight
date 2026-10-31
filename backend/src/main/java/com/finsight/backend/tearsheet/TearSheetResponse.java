package com.finsight.backend.tearsheet;

public record TearSheetResponse(
        String company,
        Integer year,
        String tearSheetMarkdown,
        String provider
) {}