package com.finsight.backend.rag;

import java.util.List;

public record RAGResponse(
        String answer,
        String provider,
        List<SourceReference> sources
) {
}