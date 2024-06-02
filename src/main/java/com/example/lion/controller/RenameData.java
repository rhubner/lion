package com.example.lion.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;

@Validated
public class RenameData {
    @Schema(description = "New name of the file", example = "dubai-fountain.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
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
