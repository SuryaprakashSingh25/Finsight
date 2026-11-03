package com.finsight.backend.mcp;

public record StreamEvent(
        String type,
        String content,
        String provider
) {
    public static StreamEvent status(String message) {
        return new StreamEvent("status", message, null);
    }

    public static StreamEvent token(String chunk) {
        return new StreamEvent("token", chunk, null);
    }

    public static StreamEvent complete(String provider) {
        return new StreamEvent("complete", "", provider);
    }

    public static StreamEvent error(String message) {
        return new StreamEvent("error", message, "error");
    }
}