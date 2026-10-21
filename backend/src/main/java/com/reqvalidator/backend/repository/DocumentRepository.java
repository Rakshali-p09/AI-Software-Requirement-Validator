package com.reqvalidator.backend.repository;

import com.reqvalidator.backend.model.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends MongoRepository<Document, String> {

    List<Document> findByProjectId(String projectId);

    Optional<Document> findByProjectIdAndFileName(
            String projectId,
            String fileName
    );
}