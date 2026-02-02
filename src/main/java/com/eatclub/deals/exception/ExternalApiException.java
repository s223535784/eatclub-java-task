package com.eatclub.deals.exception;

/**
 * Exception thrown when there's an error communicating with the external API.
 */
public class ExternalApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExternalApiException(String message) {
        super(message);
    }
    
    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}