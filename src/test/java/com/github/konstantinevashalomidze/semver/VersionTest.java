package com.github.konstantinevashalomidze.semver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

final class VersionTest {

    @Test
    void plainVersionIsParsed() {
        Version actual = Version.parse("1.4.2");
        var expectedMajor = 1;
        var expectedMinor = 4;
        var expectedPatch = 2;
        var expectedPreRelease = "";
        var expectedBuild = "";

        assertEquals(expectedMajor, actual.major());
        assertEquals(expectedMinor, actual.minor());
        assertEquals(expectedPatch, actual.patch());
        assertEquals(expectedPreRelease, actual.preRelease());
        assertEquals(expectedBuild, actual.build());
    }


    @Test
    void fullVersionIsParsed() {
        Version actual = Version.parse("1.0.0-beta.1+exp.sha.5114f85");
        var expectedMajor = 1;
        var expectedMinor = 0;
        var expectedPatch = 0;
        var expectedPreRelease = "beta.1";
        var expectedBuild = "exp.sha.5114f85";

        assertEquals(expectedMajor, actual.major());
        assertEquals(expectedMinor, actual.minor());
        assertEquals(expectedPatch, actual.patch());
        assertEquals(expectedPreRelease, actual.preRelease());
        assertEquals(expectedBuild, actual.build());
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "1.4", "1.04.2", "1.4.", "+1.4.2", "١.٢.٣", "1.0.0-beta..1",
            "1.0.0-beta.01", "1.0.0-beta_1", "1.0.0+", "1.0.0-beta+",
            "1.0.0-+build", "1.0.0+a+b", "1.0.0+a..b", "1.0.0+a_b",
    })
    void parseThrowsWhenInputIs(String param) {
        assertThrows(SemverInvalidInputException.class, () -> Version.parse(param));
    }

    @Test
    void parsesBuildVersionContainingDashes() {
        var actual = Version.parse("1.0.0+21AF26D3----117B344092BD");
        var expectedPreRelease = "";
        var expectedBuild = "21AF26D3----117B344092BD";

        assertEquals(expectedPreRelease, actual.preRelease());
        assertEquals(expectedBuild, actual.build());
    }

    @ParameterizedTest
    @CsvSource({
        "-1,0,0", "0,-1,0", "0,0,-1",
    })
    void constructorThrowsWhen(int major, int minor, int patch) {
        assertThrows(SemverInvalidInputException.class, () -> new Version(major, minor, patch, "", ""));
    }

    @Test
    void constructorThrowsWhenPreReleaseIsNull() {
        assertThrows(SemverInvalidInputException.class, () -> new Version(1, 1, 1, null, ""));
    }


    @Test
    void constructorThrowsWhenBuildIsNull() {
        assertThrows(SemverInvalidInputException.class, () -> new Version(1, 1, 1, "", null));
    }

    @Test
    void constructorThrowsWhenMajorIsNullAndMinorIsNull() {
        assertThrows(SemverInvalidInputException.class, () -> new Version(1, 1, 1, null, null));
    }

    @ParameterizedTest
    @CsvSource({
            "1.0.0-alpha,1.0.0-alpha.1",
            "1.0.0-alpha.1,1.0.0-alpha.beta",
            "1.0.0-alpha.beta,1.0.0-beta",
            "1.0.0-beta,1.0.0-beta.2",
            "1.0.0-beta.2,1.0.0-beta.11",
            "1.0.0-beta.11,1.0.0-rc.1",
            "1.0.0-rc.1,1.0.0",
            "0.0.0,0.0.1",
            "0.0.0,0.1.0",
            "0.0.0,1.0.0",
    })
    void compareToComparisonDirectionsCheck(String a, String b) {
        Version aV = Version.parse(a);
        Version bV = Version.parse(b);
        assertTrue(aV.compareTo(bV) < 0);
        assertTrue(bV.compareTo(aV) > 0);
    }

    @ParameterizedTest
    @CsvSource({
        "1.0.0,1.0.0",
        "1.0.0+a,1.0.0+b",
        "1.0.0-rc.1,1.0.0-rc.1+build.5"
    })
    void equalityCheck(String a, String b) {
        Version aV = Version.parse(a);
        Version bV = Version.parse(b);
        assertEquals(0, aV.compareTo(bV));
        assertEquals(0, bV.compareTo(aV));

        // Uses underlying overridden equals method
        assertEquals(aV, bV);
        assertEquals(bV, aV);

        assertEquals(aV.hashCode(), bV.hashCode());
    }

    @ParameterizedTest
    @CsvSource({
        "1.0.0-beta,1.0.0",
        "1.0.0,1.0.1",
        "1.0.0-alpha.1,1.0.0-alpha.01x"
    })
    void nonEqualityCheck(String a, String b) {
        var aV = Version.parse(a);
        var bV = Version.parse(b);

        assertTrue(aV.compareTo(bV) != 0);
        assertTrue(bV.compareTo(aV) != 0);

        assertNotEquals(aV, bV);
        assertNotEquals(bV, aV);
    }

    @Test
    void equalsReturnsFalse() {
        var version = Version.parse("1.0.0");
        assertEquals(Version.parse("1.0.0"), version);
        assertFalse(version.equals(null));
        assertFalse(version.equals("1.0.0"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " 1.4.2",
            "1.-4.2",
            "1.4.2.5",
            "9999999999999999.0.0"
    })
    void parseThrowsWhen(String value) {
        assertThrows(SemverInvalidInputException.class, () -> Version.parse(value));
    }

}
