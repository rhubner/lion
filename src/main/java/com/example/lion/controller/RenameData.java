package com.example.lion.controller;

public class RenameData {
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
