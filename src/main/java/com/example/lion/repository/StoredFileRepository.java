package com.example.lion.repository;

import com.example.lion.domain.StoredFile;
import org.apache.catalina.Store;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface StoredFileRepository extends MongoRepository<StoredFile, String> {

    public Optional<StoredFile> findByName(String name);

    public Optional<StoredFile> findBySha256(String sha256);


}
