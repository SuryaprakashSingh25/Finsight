package com.finsight.backend.rag;

import java.util.List;

public record RagContext(
        String context,
        List<SourceReference> sources
) {
}
