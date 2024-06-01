package com.example.lion.controller.model;

public class LionErrorResponse {

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
