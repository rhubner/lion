package com.example.lion.domain;

import org.springframework.data.annotation.Id;

public class StoredFile {

    @Id
    private String id; // FileName
    private String[] tags;
    private String sha256;

    public StoredFile() {
    }
    public StoredFile(String id) {
        this.id = id;
    }
    public StoredFile(String id, String[] tags, String sha256) {
        this.id = id;
        this.tags = tags;
        this.sha256 = sha256;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }
}
