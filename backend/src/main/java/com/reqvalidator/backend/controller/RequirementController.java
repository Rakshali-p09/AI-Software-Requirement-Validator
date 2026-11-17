
package com.reqvalidator.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reqvalidator.backend.model.Project;
import com.reqvalidator.backend.model.Requirement;
import com.reqvalidator.backend.repository.ProjectRepository;
import com.reqvalidator.backend.repository.RequirementRepository;

@RestController
@RequestMapping("/api/requirements")
@CrossOrigin(origins = "http://localhost:5173")
public class RequirementController {

    private final RequirementRepository requirementRepository;
    private final ProjectRepository projectRepository;

    public RequirementController(
            RequirementRepository requirementRepository,
            ProjectRepository projectRepository) {

        this.requirementRepository = requirementRepository;
        this.projectRepository = projectRepository;
    }

    // =========================================================
    // CREATE REQUIREMENT
    // =========================================================

    @PostMapping
    public ResponseEntity<?> createRequirement(
            @RequestBody Requirement requirement,
            Authentication authentication) {

        String authenticatedEmail = authentication.getName();

        if (requirement.getProjectId() == null
                || requirement.getProjectId().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Project ID is required.");
        }

        if (requirement.getRequirementText() == null
                || requirement.getRequirementText().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Requirement text cannot be empty.");
        }

        Project project = projectRepository
                .findById(requirement.getProjectId())
                .orElse(null);

        if (project == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Project not found.");
        }

        if (!authenticatedEmail.equalsIgnoreCase(
                project.getOwnerEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not allowed to add requirements to this project.");
        }

        long existingCount = requirementRepository
                .countByProjectId(requirement.getProjectId());

        String requirementId = String.format(
                "REQ-%03d", existingCount + 1);

        requirement.setRequirementId(requirementId);
        requirement.setRequirementText(
                requirement.getRequirementText().trim());

        Requirement savedRequirement =
                requirementRepository.save(requirement);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedRequirement);
    }

    // =========================================================
    // GET REQUIREMENTS BY PROJECT
    // =========================================================

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getRequirementsByProject(
            @PathVariable String projectId,
            Authentication authentication) {

        String authenticatedEmail = authentication.getName();

        Project project = projectRepository
                .findById(projectId)
                .orElse(null);

        if (project == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Project not found.");
        }

        if (!authenticatedEmail.equalsIgnoreCase(
                project.getOwnerEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not allowed to access this project's requirements.");
        }

        List<Requirement> requirements =
                requirementRepository.findByProjectId(projectId);

        return ResponseEntity.ok(requirements);
    }

    // =========================================================
    // GET SINGLE REQUIREMENT
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getRequirement(
            @PathVariable String id,
            Authentication authentication) {

        String authenticatedEmail = authentication.getName();

        return requirementRepository.findById(id)
                .map(requirement -> {

                    Project project = projectRepository
                            .findById(requirement.getProjectId())
                            .orElse(null);

                    if (project == null) {
                        return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body("Project not found.");
                    }

                    if (!authenticatedEmail.equalsIgnoreCase(
                            project.getOwnerEmail())) {
                        return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body("You are not allowed to access this requirement.");
                    }

                    return ResponseEntity.ok(requirement);
                })
                .orElse(
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Requirement not found."));
    }

    // =========================================================
    // UPDATE REQUIREMENT TEXT
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRequirement(
            @PathVariable String id,
            @RequestBody Requirement updatedData,
            Authentication authentication) {

        String authenticatedEmail = authentication.getName();

        return requirementRepository.findById(id)
                .map(requirement -> {

                    Project project = projectRepository
                            .findById(requirement.getProjectId())
                            .orElse(null);

                    if (project == null) {
                        return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body("Project not found.");
                    }

                    if (!authenticatedEmail.equalsIgnoreCase(
                            project.getOwnerEmail())) {
                        return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body("You are not allowed to update this requirement.");
                    }

                    if (updatedData.getRequirementText() == null
                            || updatedData.getRequirementText()
                                    .trim().isEmpty()) {
                        return ResponseEntity.badRequest()
                                .body("Requirement text cannot be empty.");
                    }

                    requirement.setRequirementText(
                            updatedData.getRequirementText().trim());

                    // Clear old validation results because the text changed.
                    requirement.setClarity(null);
                    requirement.setAmbiguity(null);
                    requirement.setCompleteness(null);
                    requirement.setConsistency(null);
                    requirement.setConsistencyWith(null);
                    requirement.setConsistencyReason(null);
                    requirement.setTestability(null);
                    requirement.setMeasurability(null);
                    requirement.setDuplicateStatus(null);
                    requirement.setDuplicateSimilarity(null);
                    requirement.setDuplicateWith(null);
                    requirement.setIssues(null);
                    requirement.setSuggestions(null);
                    requirement.setQualityScore(null);
                    requirement.setStatus(null);

                    Requirement savedRequirement =
                            requirementRepository.save(requirement);

                    return ResponseEntity.ok(savedRequirement);
                })
                .orElse(
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Requirement not found."));
    }

    // =========================================================
    // DELETE REQUIREMENT
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRequirement(
            @PathVariable String id,
            Authentication authentication) {

        String authenticatedEmail = authentication.getName();

        return requirementRepository.findById(id)
                .map(requirement -> {

                    Project project = projectRepository
                            .findById(requirement.getProjectId())
                            .orElse(null);

                    if (project == null) {
                        return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body("Project not found.");
                    }

                    if (!authenticatedEmail.equalsIgnoreCase(
                            project.getOwnerEmail())) {
                        return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body("You are not allowed to delete this requirement.");
                    }

                    requirementRepository.deleteById(id);

                    return ResponseEntity.ok(
                            "Requirement deleted successfully.");
                })
                .orElse(
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Requirement not found."));
    }
}
