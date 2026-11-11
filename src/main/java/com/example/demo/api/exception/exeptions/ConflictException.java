package com.example.demo.api.exception.exeptions;

/**
 * Конфликт состояния: дубль email/телефона, повторная операция.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
