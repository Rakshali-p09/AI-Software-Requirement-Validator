package com.reqvalidator.backend.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "requirements")
public class Requirement {

    @Id
    private String id;

    private String requirementId;

    private String projectId;
    private String requirementText;

    // Individual validation results
    private String clarity;
    private String ambiguity;
    private String completeness;

    // Consistency validation
    private String consistency;
    private String consistencyWith;
    private String consistencyReason;

    private String testability;
    private String measurability;

    // Duplicate detection
    private String duplicateStatus;
    private Double duplicateSimilarity;
    private String duplicateWith;

    // Validation explanations
    private List<String> issues;
    private List<String> suggestions;

    // Overall result
    private Integer qualityScore;
    private String status;

    public Requirement() {
    }

    public Requirement(
            String projectId,
            String requirementText,
            String clarity,
            String ambiguity,
            String completeness,
            String consistency,
            String consistencyWith,
            String consistencyReason,
            String testability,
            String measurability,
            String duplicateStatus,
            Double duplicateSimilarity,
            String duplicateWith,
            List<String> issues,
            List<String> suggestions,
            Integer qualityScore,
            String status) {

        this.projectId = projectId;
        this.requirementText = requirementText;
        this.clarity = clarity;
        this.ambiguity = ambiguity;
        this.completeness = completeness;
        this.consistency = consistency;
        this.consistencyWith = consistencyWith;
        this.consistencyReason = consistencyReason;
        this.testability = testability;
        this.measurability = measurability;
        this.duplicateStatus = duplicateStatus;
        this.duplicateSimilarity = duplicateSimilarity;
        this.duplicateWith = duplicateWith;
        this.issues = issues;
        this.suggestions = suggestions;
        this.qualityScore = qualityScore;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getRequirementText() {
        return requirementText;
    }

    public void setRequirementText(String requirementText) {
        this.requirementText = requirementText;
    }

    public String getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(String requirementId) {
        this.requirementId = requirementId;
    }

    public String getClarity() {
        return clarity;
    }

    public void setClarity(String clarity) {
        this.clarity = clarity;
    }

    public String getAmbiguity() {
        return ambiguity;
    }

    public void setAmbiguity(String ambiguity) {
        this.ambiguity = ambiguity;
    }

    public String getCompleteness() {
        return completeness;
    }

    public void setCompleteness(String completeness) {
        this.completeness = completeness;
    }

    public String getConsistency() {
        return consistency;
    }

    public void setConsistency(String consistency) {
        this.consistency = consistency;
    }

    public String getConsistencyWith() {
        return consistencyWith;
    }

    public void setConsistencyWith(String consistencyWith) {
        this.consistencyWith = consistencyWith;
    }

    public String getConsistencyReason() {
        return consistencyReason;
    }

    public void setConsistencyReason(String consistencyReason) {
        this.consistencyReason = consistencyReason;
    }

    public String getTestability() {
        return testability;
    }

    public void setTestability(String testability) {
        this.testability = testability;
    }

    public String getMeasurability() {
        return measurability;
    }

    public void setMeasurability(String measurability) {
        this.measurability = measurability;
    }

    public String getDuplicateStatus() {
        return duplicateStatus;
    }

    public void setDuplicateStatus(String duplicateStatus) {
        this.duplicateStatus = duplicateStatus;
    }

    public Double getDuplicateSimilarity() {
        return duplicateSimilarity;
    }

    public void setDuplicateSimilarity(Double duplicateSimilarity) {
        this.duplicateSimilarity = duplicateSimilarity;
    }

    public String getDuplicateWith() {
        return duplicateWith;
    }

    public void setDuplicateWith(String duplicateWith) {
        this.duplicateWith = duplicateWith;
    }

    public List<String> getIssues() {
        return issues;
    }

    public void setIssues(List<String> issues) {
        this.issues = issues;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public Integer getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(Integer qualityScore) {
        this.qualityScore = qualityScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}