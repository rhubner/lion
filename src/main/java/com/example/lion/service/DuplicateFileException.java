package com.example.lion.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.CONFLICT, reason="Duplicate file")
public class DuplicateFileException extends RuntimeException {

    private final DuplicateReason reason;

    public DuplicateFileException(DuplicateReason reason) {
        this.reason = reason;
    }

    public enum DuplicateReason {
        NAME,
        CONTENT
    }

}
