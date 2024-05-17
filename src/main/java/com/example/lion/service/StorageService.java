package com.example.lion.service;

import com.example.lion.domain.StoredFile;
import com.example.lion.domain.Visibility;
import com.example.lion.repository.StoredFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import static org.springframework.data.mongodb.core.query.Criteria.*;

import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class StorageService {

    @Value("${storage.path}" )
    private Path destination;

    @Autowired
    private StoredFileRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public void storeFile(String fileName, String[] tags, Optional<String> contentType, Visibility visibility, InputStream in) throws IOException, DuplicateFileException {
        var uuid = UUID.randomUUID();

        var fileDestination = destination.resolve(uuid.toString());

        Files.copy(in, fileDestination);

        var sha256 = calculateHash(fileDestination); //TODO - Possible to optimize - calculate sha256 when copying data to storage

        var shaDuplicityResult = repository.findBySha256(sha256);
        if(shaDuplicityResult.isPresent()) {
            throw new DuplicateFileException(DuplicateFileException.DuplicateReason.CONTENT);
        }

        var nameDuplicityResult = repository.findByName(fileName);
        if(nameDuplicityResult.isPresent()) {
            throw new DuplicateFileException(DuplicateFileException.DuplicateReason.NAME);
        }

        var file = new StoredFile(uuid.toString(), tags, visibility, fileName, Files.size(fileDestination),
                "radek", sha256, Instant.now(), contentType.orElse("application/octet-stream"));
        repository.save(file);

    }

    public Path readFile(String fileName) {
        var fileMetadata = repository.findByName(fileName);
        if(fileMetadata.isPresent()) {
            return destination.resolve(fileMetadata.get().getId());
        } else {
            throw new RuntimeException("Not found");
        }
    }

    public Optional<StoredFile> getFileMetaData(String fileName) {
        return repository.findByName(fileName);
    }

    public void rename(String oldName, String newName) {
        var renameResult = mongoTemplate.update(StoredFile.class)
                .matching(where("name").is(oldName))
                .apply(new Update().set("name", newName))
                .first();

        if(renameResult.getMatchedCount() != 1) {
            throw new RuntimeException("not found");
        }
    }

    public void delete(String fileName) throws IOException {
        //repository.deleteById(fileName);
        var fileMetadata = repository.findByName(fileName);
        if(fileMetadata.isPresent()) {
            Files.deleteIfExists(destination.resolve(fileMetadata.get().getId()));
        } else {
            throw new RuntimeException("Not found");
        }
    }

    private static String calculateHash(Path file) throws IOException {
        var xxx = new DigestUtils("sha-256");
        return xxx.digestAsHex(file.toFile());
    }

}
