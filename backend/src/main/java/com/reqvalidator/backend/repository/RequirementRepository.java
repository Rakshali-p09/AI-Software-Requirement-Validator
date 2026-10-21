package com.reqvalidator.backend.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.reqvalidator.backend.model.Requirement;

public interface RequirementRepository extends MongoRepository<Requirement, String> {

    List<Requirement> findByProjectId(String projectId);

    long countByProjectId(String projectId);
}