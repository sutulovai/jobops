package com.sutulovai.jobops.exception;

public class ConflictException extends DomainException {
    public ConflictException(ErrorCode code, String message) {
        super(code, message);
    }

    public static ConflictException emailTaken(String email) {
        return new ConflictException(ErrorCode.EMAIL_TAKEN, "Email already registered: " + email);
    }
}
