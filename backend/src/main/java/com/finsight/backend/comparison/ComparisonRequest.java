package com.finsight.backend.comparison;

public record ComparisonRequest(
    String companyA,
    Integer yearA,
    String companyB,
    Integer yearB,
    String metricFocus
) {}
