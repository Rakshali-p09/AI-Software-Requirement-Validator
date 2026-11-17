
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

    private static final double DUPLICATE_THRESHOLD = 0.70;

    public List<Requirement> detectDuplicates(
            List<Requirement> requirements) {

        if (requirements == null || requirements.isEmpty()) {
            return requirements;
        }

        for (Requirement requirement : requirements) {
            requirement.setDuplicateStatus("UNIQUE");
            requirement.setDuplicateSimilarity(0.0);
            requirement.setDuplicateWith(null);
        }

        for (int i = 0; i < requirements.size(); i++) {
            Requirement first = requirements.get(i);

            for (int j = i + 1; j < requirements.size(); j++) {
                Requirement second = requirements.get(j);

                String firstText = first.getRequirementText();
                String secondText = second.getRequirementText();

                if (hasOppositeBehavior(firstText, secondText)) {
                    continue;
                }

                double similarity = calculateSimilarity(
                        firstText, secondText);

                if (similarity >= DUPLICATE_THRESHOLD) {
                    updateBestMatch(first, second, similarity);
                    updateBestMatch(second, first, similarity);
                }
            }
        }

        return requirements;
    }

    private void updateBestMatch(
            Requirement requirement,
            Requirement matchingRequirement,
            double similarity) {

        Double currentSimilarity = requirement.getDuplicateSimilarity();

        if (currentSimilarity == null || similarity > currentSimilarity) {
            requirement.setDuplicateStatus("POTENTIAL_DUPLICATE");
            requirement.setDuplicateSimilarity(round(similarity));
            requirement.setDuplicateWith(matchingRequirement.getId());
        }
    }

    private boolean hasOppositeBehavior(
            String firstText, String secondText) {

        if (firstText == null || secondText == null) {
            return false;
        }

        String first = normalize(firstText);
        String second = normalize(secondText);

        boolean firstAllows = hasPositiveAction(first);
        boolean secondAllows = hasPositiveAction(second);

        boolean firstProhibits = hasNegativeAction(first);
        boolean secondProhibits = hasNegativeAction(second);

        return (firstAllows && secondProhibits)
                || (firstProhibits && secondAllows);
    }

    private boolean hasPositiveAction(String text) {
        return containsAny(text,
                "allow", "enable", "permit");
    }

    private boolean hasNegativeAction(String text) {
        return containsAny(text,
                "prohibit", "forbid", "prevent", "disallow")
                || text.matches(".*\\bnot\\s+(allow|enable|permit)\\b.*")
                || text.matches(".*\\b(allow|enable|permit)\\s+not\\b.*");
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.matches(".*\\b" + word + "\\b.*")) {
                return true;
            }
        }
        return false;
    }

    private double calculateSimilarity(
            String firstText, String secondText) {

        if (firstText == null || secondText == null) {
            return 0.0;
        }

        Set<String> firstTokens = tokenize(firstText);
        Set<String> secondTokens = tokenize(secondText);

        if (firstTokens.isEmpty() || secondTokens.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(firstTokens);
        intersection.retainAll(secondTokens);

        Set<String> union = new HashSet<>(firstTokens);
        union.addAll(secondTokens);

        return (double) intersection.size() / union.size();
    }

    private Set<String> tokenize(String text) {
        String normalized = normalize(text);

        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "the", "a", "an", "and", "or", "to", "of", "in",
                "on", "for", "with", "shall", "should", "must",
                "system", "be", "able", "will", "required"
        ));

        return Arrays.stream(normalized.split("\\s+"))
                .map(String::trim)
                .filter(word -> !word.isEmpty())
                .filter(word -> !stopWords.contains(word))
                .collect(Collectors.toSet());
    }

    private String normalize(String text) {
        return text.toLowerCase()
                .replaceAll("\\blet\\b", "allow")
                .replaceAll("\\bthrough\\b", "using")
                .replaceAll("\\bsign\\s+in\\b", "login")
                .replaceAll("\\bsigned\\s+in\\b", "login")
                .replaceAll("\\bsign\\s+up\\b", "register")
                .replaceAll("\\bcreate\\s+an\\s+account\\b", "register")
                .replaceAll("\\bregistration\\b", "register")
                .replaceAll("\\bauthenticate\\b", "login")
                .replaceAll("\\bauthentication\\b", "login")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
