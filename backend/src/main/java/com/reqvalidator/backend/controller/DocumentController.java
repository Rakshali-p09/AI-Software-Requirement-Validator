package com.reqvalidator.backend.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.reqvalidator.backend.model.Document;
import com.reqvalidator.backend.model.Project;
import com.reqvalidator.backend.model.Requirement;
import com.reqvalidator.backend.repository.DocumentRepository;
import com.reqvalidator.backend.repository.ProjectRepository;
import com.reqvalidator.backend.repository.RequirementRepository;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:5173")
public class DocumentController {

    private final RequirementRepository requirementRepository;
    private final DocumentRepository documentRepository;
    private final ProjectRepository projectRepository;

    public DocumentController(
            RequirementRepository requirementRepository,
            DocumentRepository documentRepository,
            ProjectRepository projectRepository) {

        this.requirementRepository = requirementRepository;
        this.documentRepository = documentRepository;
        this.projectRepository = projectRepository;
    }

    // =========================================================
    // UPLOAD DOCUMENT
    // =========================================================

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("projectId") String projectId) {

        try {

            // -------------------------------------------------
            // 1. Check empty file
            // -------------------------------------------------

            if (file.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body("Please select a file.");
            }

            // -------------------------------------------------
            // 2. Check project exists
            // -------------------------------------------------

            Project project = projectRepository
                    .findById(projectId)
                    .orElse(null);

            if (project == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Project not found.");
            }

            // -------------------------------------------------
            // 3. Check authenticated user
            // -------------------------------------------------

            String authenticatedEmail =
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication()
                            .getName();

            // -------------------------------------------------
            // 4. Check project ownership
            // -------------------------------------------------

            if (!authenticatedEmail.equalsIgnoreCase(
                    project.getOwnerEmail())) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                "You are not allowed to upload documents to this project."
                        );
            }

            // -------------------------------------------------
            // 5. Get and validate file name
            // -------------------------------------------------

            String originalFileName =
                    file.getOriginalFilename();

            if (originalFileName == null
                    || originalFileName.trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Invalid file name.");
            }

            String fileName =
                    new File(originalFileName).getName();

            // -------------------------------------------------
            // 6. Check duplicate document
            // -------------------------------------------------

            if (documentRepository
                    .findByProjectIdAndFileName(
                            projectId,
                            fileName
                    )
                    .isPresent()) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "Document already uploaded for this project: "
                                        + fileName
                        );
            }

            // -------------------------------------------------
            // 7. Get file extension
            // -------------------------------------------------

            String extension = "";

            int lastDot =
                    fileName.lastIndexOf(".");

            if (lastDot != -1) {
                extension =
                        fileName.substring(
                                lastDot + 1
                        ).toLowerCase();
            }

            // -------------------------------------------------
            // 8. Check supported file types
            // -------------------------------------------------

            if (!extension.equals("pdf")
                    && !extension.equals("doc")
                    && !extension.equals("docx")
                    && !extension.equals("txt")) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "Unsupported file type. "
                                        + "Only PDF, DOC, DOCX and TXT files are allowed."
                        );
            }

            // -------------------------------------------------
            // 9. Save physical file
            // -------------------------------------------------

            File uploadDirectory =
                    new File("uploads");

            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
            }

            File savedFile =
                    new File(
                            uploadDirectory,
                            fileName
                    );

            try (
                    FileOutputStream outputStream =
                            new FileOutputStream(savedFile)
            ) {

                outputStream.write(
                        file.getBytes()
                );
            }

            // -------------------------------------------------
            // 10. Extract text
            // -------------------------------------------------

            String extractedText;

            if (extension.equals("pdf")) {

                extractedText =
                        extractTextFromPdf(
                                file.getBytes()
                        );

            } else if (extension.equals("docx")) {

                extractedText =
                        extractTextFromDocx(
                                file.getBytes()
                        );

            } else if (extension.equals("doc")) {

                extractedText =
                        extractTextFromDoc(
                                file.getBytes()
                        );

            } else {

                extractedText =
                        new String(
                                file.getBytes(),
                                StandardCharsets.UTF_8
                        );
            }

            // -------------------------------------------------
            // 11. Extract requirements
            // -------------------------------------------------

            List<String> extractedRequirements =
                    extractRequirements(
                            extractedText
                    );

            // -------------------------------------------------
            // 12. Save document metadata
            // -------------------------------------------------

            Document document =
                    new Document();

            document.setProjectId(
                    projectId
            );

            document.setFileName(
                    fileName
            );

            document.setFileType(
                    extension
            );

            document.setFileSize(
                    file.getSize()
            );

            document.setUploadTime(
                    LocalDateTime.now().toString()
            );

            documentRepository.save(
                    document
            );

            // -------------------------------------------------
            // 13. Generate requirement IDs
            // -------------------------------------------------

            long existingCount =
                    requirementRepository
                            .countByProjectId(
                                    projectId
                            );

            int requirementNumber =
                    (int) existingCount + 1;

            int savedCount = 0;

            // -------------------------------------------------
            // 14. Save extracted requirements
            // -------------------------------------------------

            for (String requirementText :
                    extractedRequirements) {

                Requirement requirement =
                        new Requirement();

                requirement.setProjectId(
                        projectId
                );

                requirement.setRequirementId(
                        String.format(
                                "REQ-%03d",
                                requirementNumber
                        )
                );

                requirement.setRequirementText(
                        requirementText
                );

                // Validation fields
                requirement.setClarity("");
                requirement.setAmbiguity("");
                requirement.setCompleteness("");
                requirement.setConsistency("");
                requirement.setConsistencyWith(null);
                requirement.setConsistencyReason(null);
                requirement.setTestability("");
                requirement.setMeasurability("");

                // Duplicate fields
                requirement.setDuplicateStatus("");
                requirement.setDuplicateSimilarity(null);
                requirement.setDuplicateWith(null);

                // Issues
                requirement.setIssues(
                        new ArrayList<>()
                );

                // Suggestions
                requirement.setSuggestions(
                        new ArrayList<>()
                );

                // Score and status
                requirement.setQualityScore(null);
                requirement.setStatus("Pending");

                requirementRepository.save(
                        requirement
                );

                savedCount++;
                requirementNumber++;
            }

            // -------------------------------------------------
            // 15. Return response
            // -------------------------------------------------

            String response =
                    "File uploaded successfully: "
                            + fileName
                            + "\n\n"
                            + "File type: "
                            + extension
                            + "\n"
                            + "File size: "
                            + file.getSize()
                            + " bytes\n"
                            + "Upload time: "
                            + document.getUploadTime()
                            + "\n\n"
                            + "Requirements extracted: "
                            + savedCount
                            + "\n\n"
                            + "Extracted Text:\n\n"
                            + extractedText;

            return ResponseEntity.ok(
                    response
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Error processing uploaded document.",
                    e
            );
        }
    }

    // =========================================================
    // GET DOCUMENTS BY PROJECT
    // =========================================================

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getDocumentsByProject(
            @PathVariable String projectId) {

        String authenticatedEmail =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        Project project =
                projectRepository
                        .findById(projectId)
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
                    .body(
                            "You are not allowed to access this project's documents."
                    );
        }

        return ResponseEntity.ok(
                documentRepository.findByProjectId(
                        projectId
                )
        );
    }

    // =========================================================
    // PDF TEXT EXTRACTION
    // =========================================================

    private String extractTextFromPdf(
            byte[] fileBytes) throws IOException {

        try (
                PDDocument document =
                        Loader.loadPDF(fileBytes)
        ) {

            PDFTextStripper stripper =
                    new PDFTextStripper();

            return stripper.getText(
                    document
            );
        }
    }

    // =========================================================
    // DOCX TEXT EXTRACTION
    // =========================================================

    private String extractTextFromDocx(
            byte[] fileBytes) throws IOException {

        try (
                XWPFDocument document =
                        new XWPFDocument(
                                new ByteArrayInputStream(
                                        fileBytes
                                )
                        );

                XWPFWordExtractor extractor =
                        new XWPFWordExtractor(
                                document
                        )
        ) {

            return extractor.getText();
        }
    }

    // =========================================================
    // DOC TEXT EXTRACTION
    // =========================================================

    private String extractTextFromDoc(
            byte[] fileBytes) throws IOException {

        try (
                HWPFDocument document =
                        new HWPFDocument(
                                new ByteArrayInputStream(
                                        fileBytes
                                )
                        );

                WordExtractor extractor =
                        new WordExtractor(
                                document
                        )
        ) {

            return extractor.getText();
        }
    }

    // =========================================================
    // REQUIREMENT EXTRACTION
    // =========================================================

    private List<String> extractRequirements(
            String text) {

        List<String> requirements =
                new ArrayList<>();

        if (text == null
                || text.trim().isEmpty()) {

            return requirements;
        }

        String[] lines =
                text.split("\\r?\\n");

        boolean insideRequirementsSection = false;

        for (String line : lines) {

            String trimmedLine =
                    line.trim();

            if (trimmedLine.isEmpty()) {
                continue;
            }

            // -------------------------------------------------
            // Remove common bullet characters temporarily
            // -------------------------------------------------

            String cleanedLine =
                    trimmedLine.replaceFirst(
                            "^[•\\-–—*]\\s+",
                            ""
                    ).trim();

            // -------------------------------------------------
            // Remove existing requirement ID
            //
            // Example:
            // REQ-001: The system shall allow users.
            //
            // Becomes:
            // The system shall allow users.
            // -------------------------------------------------

            cleanedLine =
                    cleanedLine.replaceFirst(
                            "(?i)^REQ-\\d+\\s*:\\s*",
                            ""
                    ).trim();

            if (cleanedLine.isEmpty()) {
                continue;
            }

            String lowerCase =
                    cleanedLine.toLowerCase();

            // -------------------------------------------------
            // Detect Requirements section
            // -------------------------------------------------

            if (lowerCase.matches(
                    "^\\d+\\.?\\s*requirements\\s*$")
                    || lowerCase.matches(
                    "^requirements\\s*$")
                    || lowerCase.matches(
                    "^\\d+\\.?\\s*functional requirements\\s*$")) {

                insideRequirementsSection = true;
                continue;
            }

            // -------------------------------------------------
            // Stop at common major sections
            // -------------------------------------------------

            if (insideRequirementsSection
                    && lowerCase.matches(
                    "^\\d+\\.?\\s*(analysis|design|implementation|conclusion|references).*$")) {

                insideRequirementsSection = false;
            }

            // -------------------------------------------------
            // Formal SRS requirements
            // -------------------------------------------------

            if (lowerCase.matches(
                    ".*\\bshall\\b.*")
                    || lowerCase.matches(
                    ".*\\bshould\\b.*")
                    || lowerCase.matches(
                    ".*\\bmust\\b.*")) {

                if (cleanedLine.length() >= 10) {

                    requirements.add(
                            cleanedLine
                    );
                }

                continue;
            }

            // -------------------------------------------------
            // Bullet-style requirements
            //
            // Example:
            // • User login / signup
            // • Product browsing
            // • Search and filter
            // -------------------------------------------------

            if (insideRequirementsSection
                    && trimmedLine.matches(
                    "^[•\\-–—*]\\s+.+")) {

                if (cleanedLine.length() >= 8) {

                    requirements.add(
                            cleanedLine
                    );
                }
            }
        }

        // -------------------------------------------------
        // Remove duplicate requirements
        // while preserving original order
        // -------------------------------------------------

        List<String> uniqueRequirements =
                new ArrayList<>();

        for (String requirement :
                requirements) {

            boolean alreadyExists = false;

            for (String existing :
                    uniqueRequirements) {

                if (existing.equalsIgnoreCase(
                        requirement)) {

                    alreadyExists = true;
                    break;
                }
            }

            if (!alreadyExists) {

                uniqueRequirements.add(
                        requirement
                );
            }
        }

        return uniqueRequirements;
    }
}