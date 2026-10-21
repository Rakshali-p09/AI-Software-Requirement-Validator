package com.reqvalidator.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reqvalidator.backend.model.Project;
import com.reqvalidator.backend.model.Requirement;
import com.reqvalidator.backend.repository.ProjectRepository;
import com.reqvalidator.backend.repository.RequirementRepository;
import com.reqvalidator.backend.service.LlmAnalysisService;

@RestController
@RequestMapping("/api/llm")
@CrossOrigin(origins = "http://localhost:5173")
public class LlmAnalysisController {

    private final RequirementRepository requirementRepository;
    private final ProjectRepository projectRepository;
    private final LlmAnalysisService llmAnalysisService;

    public LlmAnalysisController(
            RequirementRepository requirementRepository,
            ProjectRepository projectRepository,
            LlmAnalysisService llmAnalysisService) {

        this.requirementRepository = requirementRepository;
        this.projectRepository = projectRepository;
        this.llmAnalysisService = llmAnalysisService;
    }

    // =========================================================
    // CHECK LLM SERVER HEALTH
    // =========================================================

    @GetMapping("/health")
    public ResponseEntity<?> checkLlmHealth() {

        try {

            llmAnalysisService.checkHealth();

            return ResponseEntity.ok(
                    Map.of(
                            "status", "UP",
                            "message", "LLM service is reachable."
                    )
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(503)
                    .body(
                            Map.of(
                                    "status", "DOWN",
                                    "message",
                                    "LLM service is currently unavailable."
                            )
                    );
        }
    }

    // =========================================================
    // ANALYZE PROJECT REQUIREMENTS WITH LLM
    // =========================================================

    @PostMapping(
            value = "/analyze/project/{projectId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> analyzeProjectWithLlm(
            @PathVariable String projectId,
            Authentication authentication) {

        String authenticatedEmail = authentication.getName();

        // -----------------------------------------------------
        // Check project exists
        // -----------------------------------------------------

        Project project = projectRepository
                .findById(projectId)
                .orElse(null);

        if (project == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        // -----------------------------------------------------
        // Check project ownership
        // -----------------------------------------------------

        if (!authenticatedEmail.equalsIgnoreCase(
                project.getOwnerEmail())) {

            return ResponseEntity
                    .status(403)
                    .body(
                            Map.of(
                                    "status", 403,
                                    "error", "Forbidden",
                                    "message",
                                    "You do not have permission to access this project."
                            )
                    );
        }

        // -----------------------------------------------------
        // Get requirements belonging to this project
        // -----------------------------------------------------

        List<Requirement> requirements =
                requirementRepository.findByProjectId(projectId);

        if (requirements.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "status", 400,
                                    "error", "Bad Request",
                                    "message",
                                    "No requirements found for this project."
                            )
                    );
        }

        // -----------------------------------------------------
        // Extract only actual requirement text
        // -----------------------------------------------------

        List<String> requirementTexts =
                requirements.stream()
                        .map(Requirement::getRequirementText)
                        .filter(text ->
                                text != null && !text.isBlank())
                        .toList();

        if (requirementTexts.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "status", 400,
                                    "error", "Bad Request",
                                    "message",
                                    "No valid requirement text found."
                            )
                    );
        }

        // -----------------------------------------------------
        // Send requirements to teammate's LLM API
        // -----------------------------------------------------

        String llmResponse =
                llmAnalysisService.analyzeRequirements(
                        requirementTexts
                );

        // -----------------------------------------------------
        // Return LLM response
        // -----------------------------------------------------

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(llmResponse);
    }
}