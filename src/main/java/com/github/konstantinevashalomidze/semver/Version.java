package com.github.konstantinevashalomidze.semver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * <p>Version is an immutable record representing a
 * (<a href="https://semver.org/">semantic versioning 2.0.0</a>).
 *
 * <p>Comparison happens by comparing <strong>major</strong>, <strong>minor</strong>, <strong>patch</strong>
 * as numbers and the first difference decides. If those are equal, a version without a pre-release is higher
 * than one with it ({@code 1.0.0-rc.1 < 1.0.0}). If both have a pre-release, compare its dot-separated identifiers
 * from left to right, and first difference decides:</p>
 * <ol>
 *     <li>Both numeric: numbers are compared ({@code beta.2 < beta.11})</li>
 *     <li>Both text: ASCII order comparison ({@code alpha < beta})</li>
 *     <li>Numeric Identifier is lower than a text one ({@code alpha.1 < alpha.beta})</li>
 *     <li>If all common ones are equal, the one with more identifiers is higher ({@code beta < beta.2})</li>
 * </ol>
 * Build part is not considered during comparison.
 * {@link #equals(Object)} and {@link #hashCode()} follow the same rules.
 *
 * @param major Represents breaking change in a library
 * @param minor Represents non-breaking features
 * @param patch Represents bug fixes
 * @param preRelease Dot separated identifiers, empty if not specified
 * @param build Dot separated identifiers, empty if not specified. Isn't considered during comparisons.
 *
 * @author <a href="https://portfolio.kosta-server.org/">Konstantine Vashalomidze</a>
 */
public record Version(int major, int minor, int patch, String preRelease, String build) implements Comparable<Version> {
    public Version {
        if (major < 0 || minor < 0 || patch < 0) {
            throw new SemverInvalidInputException("Invalid version arguments");
        }

        if (preRelease == null) {
            throw new SemverInvalidInputException("Pre-release cannot be null");
        }

        if (build == null) {
            throw new SemverInvalidInputException("Build cannot be null");
        }

        if (!validPreRelease(preRelease)) {
            throw new SemverInvalidInputException("Invalid pre-release label");
        }

        if (!validBuild(build)) {
            throw new SemverInvalidInputException("Invalid build label");
        }

    }

    private boolean validBuild(String b) {
        if (b.isEmpty()) {
            return true;
        }

        var identifiers = b.split(Pattern.quote("."), -1);
        if (Arrays.stream(identifiers).anyMatch(String::isEmpty)) {
            return false;
        }

        if (Arrays.stream(identifiers).anyMatch(s -> {
            for (char c : s.toCharArray()) {
                if (!isDigit(c) && !isEnglishLetter(c) && c != '-') {
                    return true;
                }
            }
            return false;
        })) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Version other) {
        int majorComparisonResult = Integer.compare(major, other.major);
        int minorComparisonResult = Integer.compare(minor, other.minor);
        int patchComparisonResult = Integer.compare(patch, other.patch);
        if (majorComparisonResult != 0) {
            return majorComparisonResult;
        } else if (minorComparisonResult != 0) {
            return minorComparisonResult;
        } else {
            if (patchComparisonResult == 0) {
                if (preRelease.isEmpty() && other.preRelease.isEmpty()) {
                    return 0;
                } else if (preRelease.isEmpty()) {
                    return 1;
                } else if (other.preRelease.isEmpty()) {
                    return -1;
                } else {
                    var identifiers = preRelease.split(Pattern.quote("."));
                    var otherIdentifiers = other.preRelease.split(Pattern.quote("."));
                    for (int i = 0; i < Integer.min(identifiers.length, otherIdentifiers.length); i++) {
                        var identifier = identifiers[i];
                        var otherIdentifier = otherIdentifiers[i];
                        if (isNumber(identifier) && isNumber(otherIdentifier)) {
                            if (identifier.length() < otherIdentifier.length()) {
                                return -1;
                            } else if (identifier.length() > otherIdentifier.length()) {
                                return 1;
                            } else {
                                for (int j = 0; j < identifier.length(); j++) {
                                    if (identifier.charAt(j) < otherIdentifier.charAt(j)) {
                                        return -1;
                                    }

                                    if (identifier.charAt(j) > otherIdentifier.charAt(j)) {
                                        return 1;
                                    }
                                }
                            }
                        } else if (isNumber(identifier)) {
                            return -1;
                        } else if (isNumber(otherIdentifier)) {
                            return 1;
                        } else {
                            for (int j = 0; j < Integer.min(identifiers[i].length(), otherIdentifiers[i].length()); j++) {
                                var c = identifier.charAt(j);
                                var otherC = otherIdentifier.charAt(j);
                                if (c < otherC) {
                                    return -1;
                                }

                                if (c > otherC) {
                                    return 1;
                                }
                            }

                            if (identifier.length() < otherIdentifier.length()) {
                                return -1;
                            } else if (identifier.length() > otherIdentifier.length()) {
                                return 1;
                            }
                        }
                    }

                    return Integer.compare(identifiers.length, otherIdentifiers.length);
                }
            } else {
                return patchComparisonResult;
            }
        }
    }

    /**
     * Parses string in the (<a href="https://semver.org/">semantic versioning 2.0.0</a>) format
     * {@code MAJOR.MINOR.PATCH[-PRERELEASE][+BUILD]} into a {@code Version}.
     *
     * <pre>{@code
     * Version.parse("1.4.2") // 1.4.2, no pre-release and build
     * Version.parse("1.0.0-beta.1"); // pre-release "beta.1"
     * Version.parse("1.0.0-beta.1+exp.sha.5114f"); // pre-release "beta.1", build "exp.sha.51114f
     * }</pre>
     *
     * <p>The pre-release and the build are returned without their leading
     * {@code -} and {@code +} and are empty strings when not presented. Only
     * {@code major}, {@code minor} and {@code patch} must fit into an {@code int}.
     * identifiers in the pre-release and build can be any length.</p>
     *
     *
     * @param version the text to parse
     * @return the parsed version
     * @throws SemverInvalidInputException if {@code version} is {@code null} or is not a valid
     * (<a href="https://semver.org/">semantic versioning 2.0.0</a>) version: it does not have exactly
     * three dot-separated numbers, a number has a leading zeroes or characters other than the ascii
     * digits 0-9, a number is too large for an {@code int}, a {@code -} or {@code +} is not followed
     * by text, a pre-release or build identifier is empty or contains characters other than {@code 0-9},
     * {@code A-Z}, {@code a-z} and {@code -}, or a numeric pre-release identifier has a leading zeroes.
     */
    public static Version parse(String version) throws SemverInvalidInputException {
        var hint = """
                    \nThe signature of version should be MAJOR.MINOR.PATCH-LABEL e.g. (3.42.11).
                    1. Exactly three parts, separated by dots.
                    2. Each part is a non-negative whole number.
                    3. No leading zeroes. (01 is invalid as a number)
                    """;

        if (version == null) {
            throw new SemverInvalidInputException("Invalid version number null" + hint);
        }
        var parts = findStringInBetween(version, hint);
        var versionsCollected = getNumbers(version, parts[0].split(Pattern.quote(".")), hint);
        return new Version(versionsCollected.getFirst(), versionsCollected.get(1), versionsCollected.getLast(), parts[1], parts[2]);
    }

    private static String[] findStringInBetween(String s, String hint) {
        var preRelease = "";
        var build = "";
        var version = "";
        var firstMinus = s.indexOf('-');
        var firstPlus = s.indexOf("+");
        if (firstMinus != -1 && firstPlus != -1) {
            if (firstMinus < firstPlus) {
                preRelease = s.substring(firstMinus + 1, firstPlus);
                build = s.substring(firstPlus + 1);
                version = s.substring(0, firstMinus);
            } else {
                build = s.substring(firstPlus + 1);
                version = s.substring(0, firstPlus);
            }
        } else if (firstMinus != -1) {
            preRelease = s.substring(firstMinus + 1);
            version = s.substring(0, firstMinus);
        } else if (firstPlus != -1) {
            build = s.substring(firstPlus + 1);
            version = s.substring(0, firstPlus);
        } else {
            version = s;
        }

        if (version.isEmpty()) {
            throw new SemverInvalidInputException("Invalid version number " + s + hint);
        }

        if (firstMinus != -1 && (firstPlus == -1 || firstMinus < firstPlus) && preRelease.isEmpty()) {
            throw new SemverInvalidInputException("Invalid version number " + s + hint);
        }

        if (firstPlus != -1 && build.isEmpty()) {
            throw new SemverInvalidInputException("Invalid version number " + s + hint);
        }
        return new String[] { version, preRelease, build };
    }

    private static ArrayList<Integer> getNumbers(String version, String[] dotSplitVersion,
                                                 String hint) {
        var versionsCollected = new ArrayList<Integer>();
        for (var v : dotSplitVersion) {
            try {
                if (!validNumber(v)) {
                    throw new  SemverInvalidInputException("Invalid version number " + version + hint);
                }
                var value = Integer.parseInt(v);
                versionsCollected.add(value);
            } catch (NumberFormatException e) {
                throw new SemverInvalidInputException("Invalid version number " + version + hint);
            }
        }
        if (versionsCollected.size() != 3) {
            var exceptionMessage = "Invalid version number " + version + hint;
            throw new SemverInvalidInputException(exceptionMessage);
        }
        return versionsCollected;
    }

    private static boolean validNumber(String v) {
        if ((v.startsWith("0")) && v.length() > 1) {
            return false;
        }
        return isNumber(v);
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isEnglishLetter(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }

    private static boolean isNumber(String s) {
        for (char c : s.toCharArray()) {
            if (!isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean validPreRelease(String l) {
        if (l.isEmpty()) {
            return true;
        }
        var identifiers = l.split(Pattern.quote("."), -1);
        if (Arrays.stream(identifiers).anyMatch(String::isEmpty)) {
            return false;
        }

        if (Arrays.stream(identifiers).anyMatch(s -> {
            for (char c : s.toCharArray()) {
                if (!isDigit(c) && !isEnglishLetter(c) && c != '-') {
                    return true;
                }
            }
            return false;
        })) {
            return false;
        }

        if (Arrays.stream(identifiers).anyMatch(s -> isNumber(s) && !validNumber(s))) {
            return false;
        }

        return true;
    }

    /**
     * <p>Versions are equal when they have the same precedence, build is
     * ignored ({@code 1.0.0+a == 1.0.0+b}). It is consistent with {@link #compareTo(Version)}
     * and {@link #hashCode()}.</p>
     * @param other version to compare to.
     * @return if other is null, not a version and non-equal then {@code false} otherwise {@code true}.
     */
    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof Version v) && (compareTo(v) == 0);
    }

    /**
     * Gives object's hash code back. Consistent with equals, so equal versions have equal
     * hash codes. The build is not part of the hash because {@code 1.0.0+a} and {@code 1.0.0+b}
     * would be equal but hash differently, and any hash data structure would hold both of them.
     * @return the hash code
     */
    @Override
    public int hashCode() {
        String majorCode = Integer.toString(Objects.hashCode(major));
        String minor = Integer.toString(Objects.hashCode(minor()));
        String patch = Integer.toString(Objects.hashCode(patch()));
        String preRelease = Integer.toString(Objects.hashCode(preRelease()));
        String hashCode =  majorCode + minor + patch + preRelease;
        return hashCode.hashCode();
    }


}