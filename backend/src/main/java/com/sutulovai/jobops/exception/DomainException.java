package com.sutulovai.jobops.exception;

public abstract class DomainException extends RuntimeException {
    public final ErrorCode code;

    protected DomainException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    protected DomainException(ErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
