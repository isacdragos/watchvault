package com.watchvault.watchvaultbackendspringboot.error;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
