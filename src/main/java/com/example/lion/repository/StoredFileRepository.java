package com.example.lion.repository;

import com.example.lion.domain.StoredFile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StoredFileRepository extends MongoRepository<StoredFile, String> {


}
