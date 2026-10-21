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
import com.reqvalidator.backend.repository.ProjectRepository;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:5173")
public class ProjectController {

    private final ProjectRepository projectRepository;

    public ProjectController(
            ProjectRepository projectRepository) {

        this.projectRepository = projectRepository;
    }

    // =========================================================
    // CREATE PROJECT
    // =========================================================

    @PostMapping
    public ResponseEntity<?> createProject(
            @RequestBody Project project,
            Authentication authentication) {

        // Get email directly from authenticated JWT
        String authenticatedEmail =
                authentication.getName();

        // Never trust ownerEmail sent by frontend
        project.setOwnerEmail(authenticatedEmail);

        // Default status
        if (project.getStatus() == null
                || project.getStatus().trim().isEmpty()) {

            project.setStatus("Created");
        }

        Project savedProject =
                projectRepository.save(project);

        return ResponseEntity.ok(savedProject);
    }

    // =========================================================
    // GET PROJECTS OF LOGGED-IN USER
    // =========================================================

    @GetMapping("/owner/{email}")
    public ResponseEntity<?> getProjects(
            @PathVariable String email,
            Authentication authentication) {

        String authenticatedEmail =
                authentication.getName();

        // User can only request their own projects
        if (!authenticatedEmail.equalsIgnoreCase(email)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("You are not allowed to access another user's projects.");
        }

        List<Project> projects =
                projectRepository.findByOwnerEmail(
                        authenticatedEmail
                );

        return ResponseEntity.ok(projects);
    }

    // =========================================================
    // GET SINGLE PROJECT
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getProject(
            @PathVariable String id,
            Authentication authentication) {

        String authenticatedEmail =
                authentication.getName();

        return projectRepository
                .findById(id)
                .map(project -> {

                    // Check project ownership
                    if (!authenticatedEmail.equalsIgnoreCase(
                            project.getOwnerEmail())) {

                        return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(
                                        "You are not allowed to access this project."
                                );
                    }

                    return ResponseEntity.ok(project);
                })
                .orElse(
                        ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body("Project not found.")
                );
    }

    // =========================================================
    // DELETE PROJECT
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(
            @PathVariable String id,
            Authentication authentication) {

        String authenticatedEmail =
                authentication.getName();

        return projectRepository
                .findById(id)
                .map(project -> {

                    // Check project ownership
                    if (!authenticatedEmail.equalsIgnoreCase(
                            project.getOwnerEmail())) {

                        return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(
                                        "You are not allowed to delete this project."
                                );
                    }

                    projectRepository.deleteById(id);

                    return ResponseEntity.ok(
                            "Project deleted successfully."
                    );
                })
                .orElse(
                        ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body("Project not found.")
                );
    }
}