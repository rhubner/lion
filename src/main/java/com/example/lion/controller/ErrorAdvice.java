package com.example.lion.controller;


import com.example.lion.controller.model.LionErrorResponse;
import com.example.lion.service.DuplicateFileException;
import com.example.lion.service.FileNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorAdvice {

    @ExceptionHandler(FileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public LionErrorResponse notFoundErrorHandle(FileNotFoundException fileNotFoundException) {
        return new LionErrorResponse("Requested file doesn't exist.");
    }

    @ExceptionHandler(DuplicateFileException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public LionErrorResponse duplicateErrorHandle(DuplicateFileException duplicateFileException) {
        return switch (duplicateFileException.getReason()) {
            case NAME -> new LionErrorResponse("File with same name already exists");
            case CONTENT -> new LionErrorResponse("File with same content already exists");
        };
    }
}
