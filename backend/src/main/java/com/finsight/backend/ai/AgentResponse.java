package com.finsight.backend.ai;

public record AgentResponse(
        String response,
        String provider
) {}