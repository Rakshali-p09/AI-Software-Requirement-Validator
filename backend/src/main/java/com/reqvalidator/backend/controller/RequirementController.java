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

        String authenticatedEmail =
                authentication.getName();

        // Check project exists
        Project project =
                projectRepository.findById(
                        requirement.getProjectId()
                ).orElse(null);

        if (project == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Project not found.");
        }

        // Check project ownership
        if (!authenticatedEmail.equalsIgnoreCase(
                project.getOwnerEmail())) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You are not allowed to add requirements to this project."
                    );
        }

        /*
         * Generate a human-readable requirement ID
         * based on the number of requirements already
         * present in the project.
         */

        long existingCount =
                requirementRepository.countByProjectId(
                        requirement.getProjectId()
                );

        String requirementId =
                String.format(
                        "REQ-%03d",
                        existingCount + 1
                );

        requirement.setRequirementId(
                requirementId
        );

        Requirement savedRequirement =
                requirementRepository.save(
                        requirement
                );

        return ResponseEntity.ok(
                savedRequirement
        );
    }

    // =========================================================
    // GET REQUIREMENTS BY PROJECT
    // =========================================================

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getRequirementsByProject(
            @PathVariable String projectId,
            Authentication authentication) {

        String authenticatedEmail =
                authentication.getName();

        Project project =
                projectRepository.findById(
                        projectId
                ).orElse(null);

        if (project == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Project not found.");
        }

        // Only project owner can see its requirements
        if (!authenticatedEmail.equalsIgnoreCase(
                project.getOwnerEmail())) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You are not allowed to access this project's requirements."
                    );
        }

        List<Requirement> requirements =
                requirementRepository.findByProjectId(
                        projectId
                );

        return ResponseEntity.ok(
                requirements
        );
    }

    // =========================================================
    // GET SINGLE REQUIREMENT
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getRequirement(
            @PathVariable String id,
            Authentication authentication) {

        String authenticatedEmail =
                authentication.getName();

        return requirementRepository
                .findById(id)
                .map(requirement -> {

                    Project project =
                            projectRepository.findById(
                                    requirement.getProjectId()
                            ).orElse(null);

                    if (project == null) {
                        return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(
                                        "Project not found."
                                );
                    }

                    if (!authenticatedEmail.equalsIgnoreCase(
                            project.getOwnerEmail())) {

                        return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(
                                        "You are not allowed to access this requirement."
                                );
                    }

                    return ResponseEntity.ok(
                            requirement
                    );
                })
                .orElse(
                        ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(
                                        "Requirement not found."
                                )
                );
    }

    // =========================================================
    // DELETE REQUIREMENT
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRequirement(
            @PathVariable String id,
            Authentication authentication) {

        String authenticatedEmail =
                authentication.getName();

        return requirementRepository
                .findById(id)
                .map(requirement -> {

                    Project project =
                            projectRepository.findById(
                                    requirement.getProjectId()
                            ).orElse(null);

                    if (project == null) {
                        return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(
                                        "Project not found."
                                );
                    }

                    if (!authenticatedEmail.equalsIgnoreCase(
                            project.getOwnerEmail())) {

                        return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(
                                        "You are not allowed to delete this requirement."
                                );
                    }

                    requirementRepository.deleteById(
                            id
                    );

                    return ResponseEntity.ok(
                            "Requirement deleted successfully."
                    );
                })
                .orElse(
                        ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(
                                        "Requirement not found."
                                )
                );
    }
}