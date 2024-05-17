package com.example.lion.service;

import com.example.lion.domain.StoredFile;
import com.example.lion.repository.StoredFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Component
public class StorageService {

    @Value("${storage.path}" )
    private Path destination;

    @Autowired
    private StoredFileRepository repository;

    public void storeFile(String fileName, String[] tags, InputStream in) throws IOException, DuplicateFileException {
        var file = new StoredFile(fileName, tags, "NONE");
        repository.save(file);
        Files.copy(in, destination.resolve(fileName));
    }

    public Path readFile(String fileName) {
        return destination.resolve(fileName);
    }


    public Optional<StoredFile> getFileMetaData(String fileName) {
        return repository.findById(fileName);
    }

    public void delete(String fileName) throws IOException {
        //repository.deleteById(fileName);
        Files.deleteIfExists(destination.resolve(fileName));
    }



}
