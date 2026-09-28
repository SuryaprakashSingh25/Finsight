package com.finsight.backend.ai;

public interface AIProvider {
    String generate(String prompt);
    String getProviderName();
}
