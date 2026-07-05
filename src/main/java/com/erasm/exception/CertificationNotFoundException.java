package com.erasm.exception;

public class CertificationNotFoundException extends RuntimeException {
    public CertificationNotFoundException(String message) {
        super(message);
    }
}
