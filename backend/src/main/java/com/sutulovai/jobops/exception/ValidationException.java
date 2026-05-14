package com.sutulovai.jobops.exception;

public class ValidationException extends DomainException {
    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
    }

    public static void require(boolean condition, String message) {
        if (!condition) throw new ValidationException(message);
    }
}
