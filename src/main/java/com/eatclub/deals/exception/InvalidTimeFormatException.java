package com.eatclub.deals.exception;

/**
 * Exception thrown when an invalid time format is provided.
 */
public class InvalidTimeFormatException extends RuntimeException {
    
    public InvalidTimeFormatException(String message) {
        super(message);
    }
    
    public InvalidTimeFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}