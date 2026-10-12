package org.example.core.dto.exception;

public class InsufficientProductQuantityException extends RuntimeException{
    public InsufficientProductQuantityException(String message) {
        super(message);
    }
}
