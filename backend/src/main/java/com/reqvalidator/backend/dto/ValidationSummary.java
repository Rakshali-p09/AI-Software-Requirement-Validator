package com.reqvalidator.backend.dto;

public class ValidationSummary {

    private String projectId;

    private int totalRequirements;
    private int goodRequirements;
    private int needsImprovement;
    private int poorRequirements;

    private double averageScore;

    private int duplicateRequirements;
    private int inconsistentRequirements;

    public ValidationSummary() {
    }

    public ValidationSummary(
            String projectId,
            int totalRequirements,
            int goodRequirements,
            int needsImprovement,
            int poorRequirements,
            double averageScore,
            int duplicateRequirements,
            int inconsistentRequirements) {

        this.projectId = projectId;
        this.totalRequirements = totalRequirements;
        this.goodRequirements = goodRequirements;
        this.needsImprovement = needsImprovement;
        this.poorRequirements = poorRequirements;
        this.averageScore = averageScore;
        this.duplicateRequirements = duplicateRequirements;
        this.inconsistentRequirements = inconsistentRequirements;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public int getTotalRequirements() {
        return totalRequirements;
    }

    public void setTotalRequirements(int totalRequirements) {
        this.totalRequirements = totalRequirements;
    }

    public int getGoodRequirements() {
        return goodRequirements;
    }

    public void setGoodRequirements(int goodRequirements) {
        this.goodRequirements = goodRequirements;
    }

    public int getNeedsImprovement() {
        return needsImprovement;
    }

    public void setNeedsImprovement(int needsImprovement) {
        this.needsImprovement = needsImprovement;
    }

    public int getPoorRequirements() {
        return poorRequirements;
    }

    public void setPoorRequirements(int poorRequirements) {
        this.poorRequirements = poorRequirements;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public int getDuplicateRequirements() {
        return duplicateRequirements;
    }

    public void setDuplicateRequirements(int duplicateRequirements) {
        this.duplicateRequirements = duplicateRequirements;
    }

    public int getInconsistentRequirements() {
        return inconsistentRequirements;
    }

    public void setInconsistentRequirements(int inconsistentRequirements) {
        this.inconsistentRequirements = inconsistentRequirements;
    }
}