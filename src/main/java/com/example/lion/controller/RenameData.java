package com.example.lion.controller;

import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;

@Validated
public class RenameData {
    @Pattern(regexp = FileController.FILENAME_PATTERN)
    private String newFileName;

    public RenameData() {
    }


    public RenameData(String newFileName) {
        this.newFileName = newFileName;
    }

    public String getNewFileName() {
        return newFileName;
    }

    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }
}
