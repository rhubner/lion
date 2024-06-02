package com.example.lion.controller.model;

import io.swagger.v3.oas.annotations.media.Schema;

public class LionErrorResponse {

    @Schema(description = "Detail description of the error",
            example = "File with same content already exists",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String detail;

    public LionErrorResponse() {
    }

    public LionErrorResponse(String detail) {
        this.detail = detail;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
