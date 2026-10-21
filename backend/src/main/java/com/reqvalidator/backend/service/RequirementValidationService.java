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

    /*
     * =========================================================
     * AMBIGUOUS WORDS
     * =========================================================
     */

    private static final Set<String> AMBIGUOUS_WORDS = new HashSet<>(
            Arrays.asList(
                    "quickly",
                    "fast",
                    "easily",
                    "easy",
                    "simple",
                    "user-friendly",
                    "efficiently",
                    "efficient",
                    "appropriate",
                    "reasonable",
                    "soon",
                    "adequate",
                    "good",
                    "better",
                    "normally",
                    "usually",
                    "etc",
                    "etc."
            )
    );

    /*
     * =========================================================
     * MEASUREMENT WORDS
     * =========================================================
     */

    private static final Set<String> MEASUREMENT_WORDS = new HashSet<>(
            Arrays.asList(
                    "seconds",
                    "second",
                    "milliseconds",
                    "millisecond",
                    "minutes",
                    "minute",
                    "hours",
                    "hour",
                    "percent",
                    "%",
                    "maximum",
                    "minimum",
                    "at least",
                    "at most",
                    "less than",
                    "greater than",
                    "within",
                    "limit"
            )
    );

    /*
     * =========================================================
     * OBSERVABLE ACTIONS
     * =========================================================
     *
     * These actions describe behavior that can be observed
     * during testing.
     */

    private static final Set<String> OBSERVABLE_ACTIONS = new HashSet<>(
            Arrays.asList(
                    "display",
                    "show",
                    "generate",
                    "create",
                    "delete",
                    "update",
                    "store",
                    "save",
                    "send",
                    "receive",
                    "upload",
                    "download",
                    "validate",
                    "calculate",
                    "notify",
                    "allow",
                    "prevent",
                    "reject",
                    "accept",
                    "authenticate",
                    "redirect",
                    "return",
                    "provide",
                    "process"
            )
    );

    /*
     * =========================================================
     * MAIN VALIDATION
     * =========================================================
     */

    public Requirement validate(Requirement requirement) {

        String text = requirement.getRequirementText();

        List<String> issues = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        /*
         * Empty requirement check
         */

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

            suggestions.add(
                    "Provide a clear and specific requirement."
            );

            requirement.setIssues(issues);
            requirement.setSuggestions(suggestions);

            return requirement;
        }

        text = text.trim();

        String lowerText = text.toLowerCase();

        /*
         * Run validation checks
         */

        boolean clarityPass =
                checkClarity(lowerText);

        boolean ambiguityPass =
                checkAmbiguity(lowerText);

        boolean completenessPass =
                checkCompleteness(lowerText);

        boolean testabilityPass =
                checkTestability(lowerText);

        boolean measurabilityPass =
                checkMeasurability(lowerText);

        /*
         * Store validation results
         */

        requirement.setClarity(
                clarityPass ? "PASS" : "FAIL"
        );

        requirement.setAmbiguity(
                ambiguityPass ? "PASS" : "FAIL"
        );

        requirement.setCompleteness(
                completenessPass ? "PASS" : "FAIL"
        );

        /*
         * Consistency is handled separately
         */

        requirement.setConsistency("PENDING");

        requirement.setTestability(
                testabilityPass ? "PASS" : "FAIL"
        );

        requirement.setMeasurability(
                measurabilityPass ? "PASS" : "FAIL"
        );

        /*
         * Generate feedback
         */

        generateClarityFeedback(
                lowerText,
                clarityPass,
                issues,
                suggestions
        );

        generateAmbiguityFeedback(
                lowerText,
                ambiguityPass,
                issues,
                suggestions
        );

        generateCompletenessFeedback(
                lowerText,
                completenessPass,
                issues,
                suggestions
        );

        generateTestabilityFeedback(
                lowerText,
                testabilityPass,
                issues,
                suggestions
        );

        generateMeasurabilityFeedback(
                lowerText,
                measurabilityPass,
                issues,
                suggestions
        );

        /*
         * =====================================================
         * BASE SCORE
         * =====================================================
         *
         * Clarity        = 15
         * Ambiguity      = 20
         * Completeness   = 20
         * Testability    = 15
         * Measurability  = 15
         *
         * Maximum = 85
         *
         * Consistency and duplicate points are added later
         * by ValidationController.
         */

        int score = calculateBaseScore(
                clarityPass,
                ambiguityPass,
                completenessPass,
                testabilityPass,
                measurabilityPass
        );

        requirement.setQualityScore(score);

        /*
         * Temporary status
         *
         * Final status is calculated after
         * duplicate and consistency detection.
         */

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

    /*
     * =========================================================
     * CLARITY
     * Weight: 15
     * =========================================================
     */

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

    /*
     * =========================================================
     * AMBIGUITY
     * Weight: 20
     * =========================================================
     */

    private boolean checkAmbiguity(String text) {

        String normalizedText = text.toLowerCase();

        for (String word : AMBIGUOUS_WORDS) {

            String normalizedWord =
                    word.toLowerCase().trim();

            if (normalizedWord.isEmpty()) {
                continue;
            }

            String pattern =
                    ".*\\b"
                            + Pattern.quote(normalizedWord)
                            + "\\b.*";

            if (normalizedText.matches(pattern)) {
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

            String foundWord =
                    findAmbiguousWord(text);

            issues.add(
                    "The requirement contains an ambiguous term: '"
                            + foundWord
                            + "'."
            );

            suggestions.add(
                    "Replace '"
                            + foundWord
                            + "' with a specific and measurable value or condition."
            );
        }
    }

    private String findAmbiguousWord(String text) {

        String normalizedText =
                text.toLowerCase();

        for (String word : AMBIGUOUS_WORDS) {

            String normalizedWord =
                    word.toLowerCase().trim();

            String pattern =
                    ".*\\b"
                            + Pattern.quote(normalizedWord)
                            + "\\b.*";

            if (normalizedText.matches(pattern)) {
                return word;
            }
        }

        return "unclear wording";
    }

    /*
     * =========================================================
     * COMPLETENESS
     * Weight: 20
     * =========================================================
     */

    private boolean checkCompleteness(String text) {

        boolean hasRequirementKeyword =
                text.contains("shall")
                        || text.contains("must")
                        || text.contains("should")
                        || text.contains("required to");

        if (!hasRequirementKeyword) {
            return false;
        }

        if (text.length() < 25) {
            return false;
        }

        return text.contains("allow")
                || text.contains("provide")
                || text.contains("display")
                || text.contains("generate")
                || text.contains("create")
                || text.contains("delete")
                || text.contains("update")
                || text.contains("store")
                || text.contains("validate")
                || text.contains("send")
                || text.contains("receive")
                || text.contains("upload")
                || text.contains("download")
                || text.contains("process")
                || text.contains("calculate")
                || text.contains("manage")
                || text.contains("authenticate")
                || text.contains("notify");
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

    /*
     * =========================================================
     * TESTABILITY
     * Weight: 15
     * =========================================================
     */

    private boolean checkTestability(String text) {

        /*
         * A requirement must first be written as
         * an actual requirement.
         */

        boolean hasRequirementKeyword =
                text.contains("shall")
                        || text.contains("must")
                        || text.contains("should")
                        || text.contains("required to");

        if (!hasRequirementKeyword) {
            return false;
        }

        /*
         * Ambiguous requirements are difficult to
         * verify objectively.
         */

        if (!checkAmbiguity(text)) {
            return false;
        }

        /*
         * A measurable condition makes the requirement
         * directly testable.
         */

        if (checkMeasurability(text)) {
            return true;
        }

        /*
         * Otherwise, look for an observable system action.
         */

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

    /*
     * =========================================================
     * MEASURABILITY
     * Weight: 15
     * =========================================================
     */

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

    /*
     * =========================================================
     * HELPER - WHOLE WORD CHECK
     * =========================================================
     */

    private boolean containsWholeWord(
            String text,
            String word) {

        String pattern =
                ".*\\b"
                        + Pattern.quote(word)
                        + "\\b.*";

        return text.matches(pattern);
    }

    /*
     * =========================================================
     * BASE SCORE
     * =========================================================
     */

    private int calculateBaseScore(
            boolean clarity,
            boolean ambiguity,
            boolean completeness,
            boolean testability,
            boolean measurability) {

        int score = 0;

        /*
         * Clarity - 15 points
         */

        if (clarity) {
            score += 15;
        }

        /*
         * Ambiguity - 20 points
         */

        if (ambiguity) {
            score += 20;
        }

        /*
         * Completeness - 20 points
         */

        if (completeness) {
            score += 20;
        }

        /*
         * Testability - 15 points
         */

        if (testability) {
            score += 15;
        }

        /*
         * Measurability - 15 points
         */

        if (measurability) {
            score += 15;
        }

        return score;
    }
}