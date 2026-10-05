package com.finsight.backend.ai;

public class AIProviderException extends RuntimeException{
    private final String provider;
    private final int statusCode;
    private final boolean retryable;

    public AIProviderException(
            String provider,
            int statusCode,
            boolean retryable,
            String message
    ){
        super(message);
        this.provider=provider;
        this.statusCode=statusCode;
        this.retryable=retryable;
    }

    public String getProvider(){
        return provider;
    }

    public int getStatusCode(){
        return statusCode;
    }

    public boolean isRetryable(){
        return retryable;
    }
}
