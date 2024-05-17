package com.example.lion.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.HashIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(
        collection = "storedFiles"
)
public class StoredFile {

    @Id
    private String id;
    private String[] tags;
    private Visibility visibility;
    @Indexed(unique = true)
    private String name;
    private long size;
    private String user;
    @Indexed(unique = true)
    private String sha256; //TODO - Better store like byte[] - binData in real case scenario.

    private Instant uploadDate;

    private String contentType;

    public StoredFile() {
    }
    public StoredFile(String id) {
        this.id = id;
    }

    public StoredFile(String id, String[] tags, Visibility visibility, String name, long size, String user, String sha256, Instant uploadDate, String contentType) {
        this.id = id;
        this.tags = tags;
        this.visibility = visibility;
        this.name = name;
        this.size = size;
        this.user = user;
        this.sha256 = sha256;
        this.uploadDate = uploadDate;
        this.contentType = contentType;
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

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public Instant getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Instant uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
