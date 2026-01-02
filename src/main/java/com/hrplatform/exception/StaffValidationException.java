package com.hrplatform.exception;

public class StaffValidationException extends RuntimeException {

    public StaffValidationException(String message) {
        super(message);
    }

    public StaffValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}