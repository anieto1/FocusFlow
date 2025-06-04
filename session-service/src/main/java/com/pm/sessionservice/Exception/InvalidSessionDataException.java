package com.pm.sessionservice.Exception;

public class InvalidSessionDataException extends RuntimeException {
    public InvalidSessionDataException(String message) {
        super(message);
    }
}