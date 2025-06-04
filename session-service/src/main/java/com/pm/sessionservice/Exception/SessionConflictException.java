package com.pm.sessionservice.Exception;

public class SessionConflictException extends RuntimeException {
    public SessionConflictException(String message) {
        super(message);
    }
}