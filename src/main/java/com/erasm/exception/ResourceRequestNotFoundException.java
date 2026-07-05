package com.erasm.exception;

public class ResourceRequestNotFoundException extends RuntimeException {
    public ResourceRequestNotFoundException(String message) {
        super(message);
    }
}
