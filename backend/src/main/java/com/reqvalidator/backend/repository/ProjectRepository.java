package com.reqvalidator.backend.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.reqvalidator.backend.model.Project;

public interface ProjectRepository extends MongoRepository<Project, String> {

    List<Project> findByOwnerEmail(String ownerEmail);
}