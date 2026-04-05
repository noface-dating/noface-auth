package com.duri.duriauth.exception.logging;

public class JwtKeyInitializationException extends RuntimeException {

    public JwtKeyInitializationException(String message) {
        super(message);
    }

    public JwtKeyInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
