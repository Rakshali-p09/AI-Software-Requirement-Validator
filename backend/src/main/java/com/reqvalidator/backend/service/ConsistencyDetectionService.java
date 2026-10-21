package com.reqvalidator.backend.service;

import com.reqvalidator.backend.model.Requirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ConsistencyDetectionService {

    public List<Requirement> detectConsistency(
            List<Requirement> requirements) {

        if (requirements == null || requirements.isEmpty()) {
            return requirements;
        }

        // Reset consistency information
        for (Requirement requirement : requirements) {
            requirement.setConsistency("CONSISTENT");
            requirement.setConsistencyWith(null);
            requirement.setConsistencyReason(null);
        }

        // Compare every pair of requirements
        for (int i = 0; i < requirements.size(); i++) {

            Requirement first = requirements.get(i);

            for (int j = i + 1; j < requirements.size(); j++) {

                Requirement second = requirements.get(j);

                String conflictReason = findConflict(
                        first.getRequirementText(),
                        second.getRequirementText()
                );

                if (conflictReason != null) {

                    first.setConsistency("POTENTIAL_INCONSISTENCY");
                    first.setConsistencyWith(second.getId());
                    first.setConsistencyReason(conflictReason);

                    second.setConsistency("POTENTIAL_INCONSISTENCY");
                    second.setConsistencyWith(first.getId());
                    second.setConsistencyReason(conflictReason);
                }
            }
        }

        return requirements;
    }

    private String findConflict(
            String firstText,
            String secondText) {

        if (firstText == null || secondText == null) {
            return null;
        }

        String first = firstText.toLowerCase().trim();
        String second = secondText.toLowerCase().trim();

        /*
         * ---------------------------------------------------------
         * 1. MINIMUM VALUE CONFLICT
         * ---------------------------------------------------------
         */

        Integer firstMinimum = extractMinimum(first);
        Integer secondMinimum = extractMinimum(second);

        if (firstMinimum != null
                && secondMinimum != null
                && !firstMinimum.equals(secondMinimum)) {

            if (hasCommonSubject(first, second)) {
                return "Different minimum values are specified for the same subject.";
            }
        }

        /*
         * ---------------------------------------------------------
         * 2. MAXIMUM VALUE CONFLICT
         * ---------------------------------------------------------
         */

        Integer firstMaximum = extractMaximum(first);
        Integer secondMaximum = extractMaximum(second);

        if (firstMaximum != null
                && secondMaximum != null
                && !firstMaximum.equals(secondMaximum)) {

            if (hasCommonSubject(first, second)) {
                return "Different maximum values are specified for the same subject.";
            }
        }

        /*
         * ---------------------------------------------------------
         * 3. USER PERMISSION CONFLICT
         * ---------------------------------------------------------
         */

        boolean firstOnlyAdmin = containsAny(
                first,
                "only administrators",
                "only admin",
                "administrators only"
        );

        boolean secondOnlyAdmin = containsAny(
                second,
                "only administrators",
                "only admin",
                "administrators only"
        );

        boolean firstAllUsers = containsAny(
                first,
                "all users",
                "all registered users",
                "every user",
                "any user"
        );

        boolean secondAllUsers = containsAny(
                second,
                "all users",
                "all registered users",
                "every user",
                "any user"
        );

        if ((firstOnlyAdmin && secondAllUsers)
                || (firstAllUsers && secondOnlyAdmin)) {

            if (hasCommonSubject(first, second)) {
                return "Conflicting user permission rules may exist.";
            }
        }

        /*
         * ---------------------------------------------------------
         * 4. ALLOW / PROHIBIT CONFLICT
         * ---------------------------------------------------------
         */

        boolean firstAllows = containsAny(
                first,
                "shall allow",
                "must allow",
                "should allow",
                "shall enable",
                "must enable",
                "should enable"
        );

        boolean secondProhibits = containsAny(
                second,
                "shall not allow",
                "must not allow",
                "should not allow",
                "shall disable",
                "must disable",
                "should disable",
                "not allow"
        );

        boolean secondAllows = containsAny(
                second,
                "shall allow",
                "must allow",
                "should allow",
                "shall enable",
                "must enable",
                "should enable"
        );

        boolean firstProhibits = containsAny(
                first,
                "shall not allow",
                "must not allow",
                "should not allow",
                "shall disable",
                "must disable",
                "should disable",
                "not allow"
        );

        if ((firstAllows && secondProhibits)
                || (firstProhibits && secondAllows)) {

            if (hasCommonSubject(first, second)) {
                return "One requirement allows a behavior while another prohibits it.";
            }
        }

        return null;
    }

    /*
     * ---------------------------------------------------------
     * Extract minimum value
     * ---------------------------------------------------------
     */

    private Integer extractMinimum(String text) {

        Pattern pattern = Pattern.compile(
                "(?:minimum|min|at least)\\s*(?:of\\s*)?(\\d+)"
        );

        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return null;
    }

    /*
     * ---------------------------------------------------------
     * Extract maximum value
     * ---------------------------------------------------------
     */

    private Integer extractMaximum(String text) {

        Pattern pattern = Pattern.compile(
                "(?:maximum|max|at most)\\s*(?:of\\s*)?(\\d+)"
        );

        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return null;
    }

    /*
     * ---------------------------------------------------------
     * Check whether requirements talk about a similar subject
     * ---------------------------------------------------------
     */

    private boolean hasCommonSubject(
            String first,
            String second) {

        String subject1 = extractSubject(first);
        String subject2 = extractSubject(second);

        if (subject1.isEmpty() || subject2.isEmpty()) {
            return false;
        }

        return subject1.equals(subject2)
                || subject1.contains(subject2)
                || subject2.contains(subject1);
    }

    /*
     * ---------------------------------------------------------
     * Extract important subject words
     * ---------------------------------------------------------
     */

    private String extractSubject(String text) {

        String normalized = text
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ");

        /*
         * Remove common requirement words.
         */
        normalized = normalized
                .replaceAll("\\bthe\\b", " ")
                .replaceAll("\\bsystem\\b", " ")
                .replaceAll("\\bshall\\b", " ")
                .replaceAll("\\bshould\\b", " ")
                .replaceAll("\\bmust\\b", " ")
                .replaceAll("\\ballow\\b", " ")
                .replaceAll("\\busers\\b", " ")
                .replaceAll("\\buser\\b", " ")
                .replaceAll("\\bbe\\b", " ")
                .replaceAll("\\bable\\b", " ")
                .replaceAll("\\bto\\b", " ")
                .replaceAll("\\bwith\\b", " ")
                .replaceAll("\\ba\\b", " ")
                .replaceAll("\\ban\\b", " ")
                .replaceAll("\\bof\\b", " ")
                .replaceAll("\\bfor\\b", " ")
                .replaceAll("\\bminimum\\b", " ")
                .replaceAll("\\bmaximum\\b", " ")
                .replaceAll("\\bmin\\b", " ")
                .replaceAll("\\bmax\\b", " ")
                .replaceAll("\\bat\\s+least\\b", " ")
                .replaceAll("\\bat\\s+most\\b", " ");

        /*
         * Remove numbers.
         */
        normalized = normalized.replaceAll("\\b\\d+\\b", " ");

        /*
         * Remove common action words.
         */
        normalized = normalized
                .replaceAll("\\busing\\b", " ")
                .replaceAll("\\bshall\\b", " ")
                .replaceAll("\\bshould\\b", " ")
                .replaceAll("\\bmust\\b", " ");

        return normalized
                .replaceAll("\\s+", " ")
                .trim();
    }

    /*
     * ---------------------------------------------------------
     * Check multiple phrases
     * ---------------------------------------------------------
     */

    private boolean containsAny(
            String text,
            String... phrases) {

        for (String phrase : phrases) {

            if (text.contains(phrase)) {
                return true;
            }
        }

        return false;
    }
}