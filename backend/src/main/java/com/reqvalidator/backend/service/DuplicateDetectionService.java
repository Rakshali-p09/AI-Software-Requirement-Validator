package com.reqvalidator.backend.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.reqvalidator.backend.model.Requirement;

@Service
public class DuplicateDetectionService {

    /*
     * Requirements with similarity >= 0.80
     * are flagged as potential duplicates.
     */
    private static final double DUPLICATE_THRESHOLD = 0.80;

    /**
     * Analyze all requirements of a project.
     */
    public List<Requirement> detectDuplicates(
            List<Requirement> requirements) {

        if (requirements == null || requirements.isEmpty()) {
            return requirements;
        }

        /*
         * Reset duplicate information before every analysis.
         */
        for (Requirement requirement : requirements) {

            requirement.setDuplicateStatus("UNIQUE");
            requirement.setDuplicateSimilarity(0.0);
            requirement.setDuplicateWith(null);
        }

        /*
         * Compare every unique pair.
         */
        for (int i = 0; i < requirements.size(); i++) {

            Requirement first = requirements.get(i);

            for (int j = i + 1; j < requirements.size(); j++) {

                Requirement second = requirements.get(j);

                double similarity = calculateSimilarity(
                        first.getRequirementText(),
                        second.getRequirementText()
                );

                if (similarity >= DUPLICATE_THRESHOLD) {

                    /*
                     * Update first requirement.
                     */
                    if (first.getDuplicateSimilarity() == null
                            || similarity > first.getDuplicateSimilarity()) {

                        first.setDuplicateStatus(
                                "POTENTIAL_DUPLICATE"
                        );

                        first.setDuplicateSimilarity(
                                round(similarity)
                        );

                        first.setDuplicateWith(
                                second.getId()
                        );
                    }

                    /*
                     * Update second requirement.
                     */
                    if (second.getDuplicateSimilarity() == null
                            || similarity > second.getDuplicateSimilarity()) {

                        second.setDuplicateStatus(
                                "POTENTIAL_DUPLICATE"
                        );

                        second.setDuplicateSimilarity(
                                round(similarity)
                        );

                        second.setDuplicateWith(
                                first.getId()
                        );
                    }
                }
            }
        }

        return requirements;
    }

    /**
     * Calculate Jaccard similarity.
     */
    private double calculateSimilarity(
            String firstText,
            String secondText) {

        if (firstText == null || secondText == null) {
            return 0.0;
        }

        Set<String> firstTokens =
                tokenize(firstText);

        Set<String> secondTokens =
                tokenize(secondText);

        if (firstTokens.isEmpty()
                || secondTokens.isEmpty()) {

            return 0.0;
        }

        Set<String> intersection =
                new HashSet<>(firstTokens);

        intersection.retainAll(secondTokens);

        Set<String> union =
                new HashSet<>(firstTokens);

        union.addAll(secondTokens);

        return (double) intersection.size()
                / union.size();
    }

    /**
     * Normalize requirement text and create tokens.
     */
    private Set<String> tokenize(String text) {

        String normalized = text.toLowerCase();

        /*
         * Normalize common equivalent phrases.
         *
         * This allows:
         *
         * login
         * sign in
         *
         * to be treated as the same concept.
         */
        normalized = normalized
                .replaceAll("\\bsign\\s+in\\b", "login")
                .replaceAll("\\bsigned\\s+in\\b", "login")
                .replaceAll("\\bsign\\s+up\\b", "register")
                .replaceAll("\\bcreate\\s+an\\s+account\\b", "register")
                .replaceAll("\\bregistration\\b", "register")
                .replaceAll("\\bauthenticate\\b", "login")
                .replaceAll("\\bauthentication\\b", "login");

        /*
         * Remove punctuation.
         */
        normalized = normalized
                .replaceAll("[^a-z0-9\\s]", " ");

        String[] words =
                normalized.split("\\s+");

        /*
         * Common words that do not contribute much
         * to duplicate detection.
         */
        Set<String> stopWords = new HashSet<>(
                Arrays.asList(
                        "the",
                        "a",
                        "an",
                        "and",
                        "or",
                        "to",
                        "of",
                        "in",
                        "on",
                        "for",
                        "with",
                        "shall",
                        "should",
                        "must",
                        "system",
                        "be",
                        "able",
                        "will",
                        "required"
                )
        );

        return Arrays.stream(words)
                .map(String::trim)
                .filter(word -> !word.isEmpty())
                .filter(word -> !stopWords.contains(word))
                .collect(Collectors.toSet());
    }

    /**
     * Round similarity to two decimal places.
     */
    private double round(double value) {

        return Math.round(value * 100.0) / 100.0;
    }
}