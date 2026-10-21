package com.reqvalidator.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reqvalidator.backend.dto.ValidationSummary;
import com.reqvalidator.backend.model.Project;
import com.reqvalidator.backend.model.Requirement;
import com.reqvalidator.backend.repository.ProjectRepository;
import com.reqvalidator.backend.repository.RequirementRepository;
import com.reqvalidator.backend.service.ConsistencyDetectionService;
import com.reqvalidator.backend.service.DuplicateDetectionService;
import com.reqvalidator.backend.service.RequirementValidationService;

@RestController
@RequestMapping("/api/validation")
@CrossOrigin(origins = "http://localhost:5173")
public class ValidationController {

    private final RequirementRepository requirementRepository;
    private final ProjectRepository projectRepository;
    private final RequirementValidationService validationService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final ConsistencyDetectionService consistencyDetectionService;

    public ValidationController(
            RequirementRepository requirementRepository,
            ProjectRepository projectRepository,
            RequirementValidationService validationService,
            DuplicateDetectionService duplicateDetectionService,
            ConsistencyDetectionService consistencyDetectionService) {

        this.requirementRepository = requirementRepository;
        this.projectRepository = projectRepository;
        this.validationService = validationService;
        this.duplicateDetectionService = duplicateDetectionService;
        this.consistencyDetectionService = consistencyDetectionService;
    }

    // =========================================================
    // VALIDATE INDIVIDUAL REQUIREMENT
    // =========================================================

    @PostMapping("/requirement/{id}")
    public ResponseEntity<?> validateRequirement(
            @PathVariable String id,
            Authentication authentication) {

        String authenticatedEmail =
                authentication.getName();

        return requirementRepository.findById(id)
                .map(requirement -> {

                    // Check project ownership
                    Project project =
                            projectRepository.findById(
                                    requirement.getProjectId()
                            ).orElse(null);

                    if (project == null) {

                        return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body("Project not found.");
                    }

                    if (!authenticatedEmail.equalsIgnoreCase(
                            project.getOwnerEmail())) {

                        return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(
                                        "You are not allowed to validate this requirement."
                                );
                    }

                    Requirement validatedRequirement =
                            validationService.validate(
                                    requirement
                            );

                    /*
                     * Individual validation cannot reliably determine
                     * project-level duplicate and consistency status.
                     */
                    validatedRequirement.setConsistency(
                            "PENDING"
                    );

                    validatedRequirement.setConsistencyWith(
                            null
                    );

                    validatedRequirement.setConsistencyReason(
                            null
                    );

                    validatedRequirement.setDuplicateStatus(
                            "PENDING"
                    );

                    validatedRequirement.setDuplicateSimilarity(
                            null
                    );

                    validatedRequirement.setDuplicateWith(
                            null
                    );

                    int baseScore =
                            calculateBaseScore(
                                    validatedRequirement
                            );

                    validatedRequirement.setQualityScore(
                            baseScore
                    );

                    updateStatus(
                            validatedRequirement,
                            baseScore
                    );

                    Requirement savedRequirement =
                            requirementRepository.save(
                                    validatedRequirement
                            );

                    return ResponseEntity.ok(
                            savedRequirement
                    );
                })
                .orElse(
                        ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body("Requirement not found.")
                );
    }

    // =========================================================
    // VALIDATE ENTIRE PROJECT
    // =========================================================

    @PostMapping("/project/{projectId}")
    public ResponseEntity<?> validateProject(
            @PathVariable String projectId,
            Authentication authentication) {

        // -----------------------------------------------------
        // Check project ownership
        // -----------------------------------------------------

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

        if (!authenticatedEmail.equalsIgnoreCase(
                project.getOwnerEmail())) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You are not allowed to validate this project."
                    );
        }

        // -----------------------------------------------------
        // Get requirements
        // -----------------------------------------------------

        List<Requirement> requirements =
                requirementRepository.findByProjectId(
                        projectId
                );

        if (requirements.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "No requirements found for this project."
                    );
        }

        /*
         * STEP 1:
         * Basic requirement validation
         */

        requirements =
                requirements.stream()
                        .map(validationService::validate)
                        .toList();

        /*
         * STEP 2:
         * Duplicate detection
         */

        requirements =
                duplicateDetectionService
                        .detectDuplicates(
                                requirements
                        );

        /*
         * STEP 3:
         * Consistency detection
         */

        requirements =
                consistencyDetectionService
                        .detectConsistency(
                                requirements
                        );

        /*
         * STEP 4:
         * Calculate final score
         */

        for (Requirement requirement :
                requirements) {

            int finalScore =
                    calculateFinalScore(
                            requirement
                    );

            requirement.setQualityScore(
                    finalScore
            );

            updateStatus(
                    requirement,
                    finalScore
            );
        }

        /*
         * STEP 5:
         * Save results
         */

        List<Requirement> savedRequirements =
                requirements.stream()
                        .map(requirementRepository::save)
                        .toList();

        return ResponseEntity.ok(
                savedRequirements
        );
    }

    // =========================================================
    // VALIDATION SUMMARY
    // =========================================================

    @GetMapping("/project/{projectId}/summary")
    public ResponseEntity<?> getValidationSummary(
            @PathVariable String projectId,
            Authentication authentication) {

        // -----------------------------------------------------
        // Check project ownership
        // -----------------------------------------------------

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

        if (!authenticatedEmail.equalsIgnoreCase(
                project.getOwnerEmail())) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You are not allowed to access this validation summary."
                    );
        }

        // -----------------------------------------------------
        // Get requirements
        // -----------------------------------------------------

        List<Requirement> requirements =
                requirementRepository.findByProjectId(
                        projectId
                );

        if (requirements.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "No requirements found for this project."
                    );
        }

        int totalRequirements =
                requirements.size();

        int goodRequirements = 0;
        int needsImprovement = 0;
        int poorRequirements = 0;

        int duplicateRequirements = 0;
        int inconsistentRequirements = 0;

        double totalScore = 0;
        int scoredRequirements = 0;

        for (Requirement requirement :
                requirements) {

            /*
             * Count status
             */

            if ("Good".equalsIgnoreCase(
                    requirement.getStatus())) {

                goodRequirements++;

            } else if ("Needs Improvement".equalsIgnoreCase(
                    requirement.getStatus())) {

                needsImprovement++;

            } else if ("Poor".equalsIgnoreCase(
                    requirement.getStatus())) {

                poorRequirements++;
            }

            /*
             * Count duplicates
             */

            if ("POTENTIAL_DUPLICATE".equalsIgnoreCase(
                    requirement.getDuplicateStatus())) {

                duplicateRequirements++;
            }

            /*
             * Count inconsistencies
             */

            if ("POTENTIAL_INCONSISTENCY".equalsIgnoreCase(
                    requirement.getConsistency())) {

                inconsistentRequirements++;
            }

            /*
             * Calculate average score
             */

            if (requirement.getQualityScore() != null) {

                totalScore +=
                        requirement.getQualityScore();

                scoredRequirements++;
            }
        }

        double averageScore = 0;

        if (scoredRequirements > 0) {

            averageScore =
                    totalScore / scoredRequirements;

            /*
             * Round to two decimal places.
             */

            averageScore =
                    Math.round(
                            averageScore * 100.0
                    ) / 100.0;
        }

        ValidationSummary summary =
                new ValidationSummary(
                        projectId,
                        totalRequirements,
                        goodRequirements,
                        needsImprovement,
                        poorRequirements,
                        averageScore,
                        duplicateRequirements,
                        inconsistentRequirements
                );

        return ResponseEntity.ok(
                summary
        );
    }

    // =========================================================
    // BASE SCORE
    // =========================================================

    /*
     * Clarity        = 15
     * Ambiguity      = 20
     * Completeness   = 20
     * Testability    = 15
     * Measurability  = 15
     *
     * Maximum = 85
     */

    private int calculateBaseScore(
            Requirement requirement) {

        int score = 0;

        // Clarity - 15 points
        if ("PASS".equals(
                requirement.getClarity())) {

            score += 15;
        }

        // Ambiguity - 20 points
        if ("PASS".equals(
                requirement.getAmbiguity())) {

            score += 20;
        }

        // Completeness - 20 points
        if ("PASS".equals(
                requirement.getCompleteness())) {

            score += 20;
        }

        // Testability - 15 points
        if ("PASS".equals(
                requirement.getTestability())) {

            score += 15;
        }

        // Measurability - 15 points
        if ("PASS".equals(
                requirement.getMeasurability())) {

            score += 15;
        }

        return score;
    }

    // =========================================================
    // FINAL SCORE
    // =========================================================

    /*
     * Base validation = 85
     * Consistency     = 10
     * Duplicate       = 5
     *
     * Maximum = 100
     */

    private int calculateFinalScore(
            Requirement requirement) {

        int score =
                calculateBaseScore(
                        requirement
                );

        // Consistency: 10 points
        if ("CONSISTENT".equals(
                requirement.getConsistency())) {

            score += 10;
        }

        // Uniqueness: 5 points
        if ("UNIQUE".equals(
                requirement.getDuplicateStatus())) {

            score += 5;
        }

        return Math.min(
                score,
                100
        );
    }

    // =========================================================
    // STATUS
    // =========================================================

    private void updateStatus(
            Requirement requirement,
            int score) {

        if (score >= 90) {

            requirement.setStatus(
                    "Good"
            );

        } else if (score >= 70) {

            requirement.setStatus(
                    "Needs Improvement"
            );

        } else {

            requirement.setStatus(
                    "Poor"
            );
        }
    }
}