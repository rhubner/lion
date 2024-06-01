package com.example.lion.service;

public class DuplicateFileException extends RuntimeException {

    private final DuplicateReason reason;

    public DuplicateFileException(DuplicateReason reason) {
        this.reason = reason;
    }

    public DuplicateReason getReason() {
        return reason;
    }

    public enum DuplicateReason {
        NAME,
        CONTENT
    }

}
