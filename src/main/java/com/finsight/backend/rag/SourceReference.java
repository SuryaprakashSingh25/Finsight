package com.finsight.backend.rag;

public record SourceReference(
        String document,
        Integer page,
        Integer chunkIndex
) {
}