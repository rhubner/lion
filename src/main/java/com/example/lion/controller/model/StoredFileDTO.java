package com.example.lion.controller.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Search output model")
public class StoredFileDTO {

    @Schema(description = "Name of the file", example = "burj-khalifa.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    @Schema(description = "Up to 5 user defined tags. or empty array", example = "[\"downtown\", \"holiday\", \"UAE\"]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String[] tags;
    @Schema(description = "Autodetected or user defined content type.", example = "image/jpeg", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contentType;
    @Schema(description = "Upload timestamp", example = "2024-04-13T08:30:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant uploadDate;
    @Schema(description = "URL for file download", example = "http://localhost/file/burj-khalifa.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
    private String url;
    @Schema(description = "File size", example = "1024", requiredMode = Schema.RequiredMode.REQUIRED)
    private long size;

    public StoredFileDTO() {
    }

    public StoredFileDTO(String name, String[] tags, String contentType, Instant uploadDate, String url, long size) {
        this.name = name;
        this.tags = tags;
        this.contentType = contentType;
        this.uploadDate = uploadDate;
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

    public Instant getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Instant uploadDate) {
        this.uploadDate = uploadDate;
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
