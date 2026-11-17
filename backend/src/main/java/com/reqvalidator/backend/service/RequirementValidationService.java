
package com.reqvalidator.backend.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.reqvalidator.backend.model.Requirement;

@Service
public class RequirementValidationService {

    private static final Set<String> AMBIGUOUS_WORDS = new HashSet<>(
            Arrays.asList(
                    "quickly", "fast", "easily", "easy", "simple",
                    "user-friendly", "efficiently", "efficient",
                    "appropriate", "reasonable", "soon", "adequate",
                    "good", "better", "normally", "usually", "etc", "etc."
            )
    );

    private static final Set<String> MEASUREMENT_WORDS = new HashSet<>(
            Arrays.asList(
                    "seconds", "second", "milliseconds", "millisecond",
                    "minutes", "minute", "hours", "hour", "percent", "%",
                    "maximum", "minimum", "at least", "at most",
                    "less than", "greater than", "within", "limit"
            )
    );

    private static final Set<String> OBSERVABLE_ACTIONS = new HashSet<>(
            Arrays.asList(
                    "display", "show", "generate", "create", "delete",
                    "update", "store", "save", "send", "receive", "upload",
                    "download", "validate", "calculate", "notify", "allow",
                    "prevent", "reject", "accept", "authenticate",
                    "redirect", "return", "provide", "process", "respond"
            )
    );

    public Requirement validate(Requirement requirement) {

        String text = requirement.getRequirementText();

        List<String> issues = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        if (text == null || text.isBlank()) {
            requirement.setClarity("FAIL");
            requirement.setAmbiguity("FAIL");
            requirement.setCompleteness("FAIL");
            requirement.setConsistency("PENDING");
            requirement.setTestability("FAIL");
            requirement.setMeasurability("FAIL");

            requirement.setQualityScore(0);
            requirement.setStatus("Poor");

            issues.add("The requirement text is empty.");
            suggestions.add("Provide a clear and specific requirement.");

            requirement.setIssues(issues);
            requirement.setSuggestions(suggestions);

            return requirement;
        }

        text = text.trim().toLowerCase();

        boolean clarityPass = checkClarity(text);
        boolean ambiguityPass = checkAmbiguity(text);
        boolean completenessPass = checkCompleteness(text);
        boolean testabilityPass = checkTestability(text);
        boolean measurabilityPass = checkMeasurability(text);

        requirement.setClarity(clarityPass ? "PASS" : "FAIL");
        requirement.setAmbiguity(ambiguityPass ? "PASS" : "FAIL");
        requirement.setCompleteness(completenessPass ? "PASS" : "FAIL");
        requirement.setConsistency("PENDING");
        requirement.setTestability(testabilityPass ? "PASS" : "FAIL");
        requirement.setMeasurability(measurabilityPass ? "PASS" : "FAIL");

        generateClarityFeedback(text, clarityPass, issues, suggestions);
        generateAmbiguityFeedback(text, ambiguityPass, issues, suggestions);
        generateCompletenessFeedback(text, completenessPass, issues, suggestions);
        generateTestabilityFeedback(text, testabilityPass, issues, suggestions);
        generateMeasurabilityFeedback(text, measurabilityPass, issues, suggestions);

        int score = calculateBaseScore(
                clarityPass,
                ambiguityPass,
                completenessPass,
                testabilityPass,
                measurabilityPass
        );

        requirement.setQualityScore(score);

        if (score >= 68) {
            requirement.setStatus("Good");
        } else if (score >= 51) {
            requirement.setStatus("Needs Improvement");
        } else {
            requirement.setStatus("Poor");
        }

        requirement.setIssues(issues);
        requirement.setSuggestions(suggestions);

        return requirement;
    }

    // CLARITY

    private boolean checkClarity(String text) {
        if (text.length() < 15) {
            return false;
        }

        return text.contains("shall")
                || text.contains("must")
                || text.contains("should")
                || text.contains("required to")
                || text.contains("needs to");
    }

    private void generateClarityFeedback(
            String text,
            boolean passed,
            List<String> issues,
            List<String> suggestions) {

        if (!passed) {
            issues.add(
                    "The requirement is not clearly expressed as a system requirement."
            );

            suggestions.add(
                    "Use a clear requirement statement with words such as 'shall' or 'must' and describe the expected system behavior."
            );
        }
    }

    // AMBIGUITY

    private boolean checkAmbiguity(String text) {
        for (String word : AMBIGUOUS_WORDS) {
            if (containsWholeWord(text, word)) {
                return false;
            }
        }

        return true;
    }

    private void generateAmbiguityFeedback(
            String text,
            boolean passed,
            List<String> issues,
            List<String> suggestions) {

        if (!passed) {
            String foundWord = findAmbiguousWord(text);

            issues.add(
                    "The requirement contains an ambiguous term: '"
                            + foundWord + "'."
            );

            suggestions.add(
                    "Replace '" + foundWord
                            + "' with a specific and measurable value or condition."
            );
        }
    }

    private String findAmbiguousWord(String text) {
        for (String word : AMBIGUOUS_WORDS) {
            if (containsWholeWord(text, word)) {
                return word;
            }
        }

        return "unclear wording";
    }

    // COMPLETENESS

    private boolean checkCompleteness(String text) {

        boolean hasRequirementKeyword =
                text.contains("shall")
                        || text.contains("must")
                        || text.contains("should")
                        || text.contains("required to")
                        || text.contains("needs to");

        if (!hasRequirementKeyword) {
            return false;
        }

        if (text.length() < 25) {
            return false;
        }

        return containsAnyWholeWord(
                text,
                Arrays.asList(
                        "allow", "provide", "display", "generate",
                        "create", "delete", "update", "store",
                        "validate", "send", "receive", "upload",
                        "download", "process", "calculate", "manage",
                        "authenticate", "notify", "respond"
                )
        );
    }

    private void generateCompletenessFeedback(
            String text,
            boolean passed,
            List<String> issues,
            List<String> suggestions) {

        if (!passed) {
            issues.add(
                    "The requirement may be incomplete because it does not clearly describe the expected system behavior."
            );

            suggestions.add(
                    "Specify the actor, system action, object, and expected outcome."
            );
        }
    }

    // TESTABILITY

    private boolean checkTestability(String text) {

        boolean hasRequirementKeyword =
                text.contains("shall")
                        || text.contains("must")
                        || text.contains("should")
                        || text.contains("required to")
                        || text.contains("needs to");

        if (!hasRequirementKeyword) {
            return false;
        }

        if (!checkAmbiguity(text)) {
            return false;
        }

        if (checkMeasurability(text)) {
            return true;
        }

        for (String action : OBSERVABLE_ACTIONS) {
            if (containsWholeWord(text, action)) {
                return true;
            }
        }

        return false;
    }

    private void generateTestabilityFeedback(
            String text,
            boolean passed,
            List<String> issues,
            List<String> suggestions) {

        if (!passed) {
            issues.add(
                    "The requirement may be difficult to verify objectively."
            );

            suggestions.add(
                    "Define a clear condition, measurable threshold, or observable expected result that can be tested."
            );
        }
    }

    // MEASURABILITY

    private boolean checkMeasurability(String text) {
        for (String word : MEASUREMENT_WORDS) {
            if (text.contains(word)) {
                return true;
            }
        }

        return false;
    }

    private void generateMeasurabilityFeedback(
            String text,
            boolean passed,
            List<String> issues,
            List<String> suggestions) {

        if (!passed) {
            issues.add(
                    "The requirement does not contain a clear measurable condition."
            );

            suggestions.add(
                    "Add a measurable value, limit, threshold, time, quantity, or clearly observable outcome."
            );
        }
    }

    // HELPERS

    private boolean containsAnyWholeWord(String text, List<String> words) {
        for (String word : words) {
            if (containsWholeWord(text, word)) {
                return true;
            }
        }

        return false;
    }

    private boolean containsWholeWord(String text, String word) {
        String pattern = ".*\\b" + Pattern.quote(word) + "\\b.*";
        return text.matches(pattern);
    }

    // BASE SCORE

    private int calculateBaseScore(
            boolean clarity,
            boolean ambiguity,
            boolean completeness,
            boolean testability,
            boolean measurability) {

        int score = 0;

        if (clarity) {
            score += 15;
        }

        if (ambiguity) {
            score += 20;
        }

        if (completeness) {
            score += 20;
        }

        if (testability) {
            score += 15;
        }

        if (measurability) {
            score += 15;
        }

        return score;
    }
}
