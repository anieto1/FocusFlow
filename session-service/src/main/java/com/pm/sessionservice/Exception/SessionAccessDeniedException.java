package com.pm.sessionservice.Exception;

public class SessionAccessDeniedException extends RuntimeException {
    public SessionAccessDeniedException(String message) {
        super(message);
    }
}