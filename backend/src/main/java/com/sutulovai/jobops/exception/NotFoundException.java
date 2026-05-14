package com.sutulovai.jobops.exception;

public class NotFoundException extends DomainException {
    public NotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }

    public static NotFoundException forEntity(String entity, Object id) {
        return new NotFoundException(entity + " not found: " + id);
    }
}
