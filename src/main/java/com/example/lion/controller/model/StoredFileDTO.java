package com.example.lion.controller.model;

import java.time.Instant;

public class StoredFileDTO {

    private String name;
    private String[] tags;
    private String contentType;
    private Instant uploadDare;
    private String url;
    private long size;

    public StoredFileDTO() {
    }

    public StoredFileDTO(String name, String[] tags, String contentType, Instant uploadDare, String url, long size) {
        this.name = name;
        this.tags = tags;
        this.contentType = contentType;
        this.uploadDare = uploadDare;
        this.url = url;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Instant getUploadDare() {
        return uploadDare;
    }

    public void setUploadDare(Instant uploadDare) {
        this.uploadDare = uploadDare;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
