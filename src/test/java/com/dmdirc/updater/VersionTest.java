/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.updater;

import org.junit.Test;

import static org.junit.Assert.*;

public class VersionTest {

    @Test
    public void testConstructorWithNoArgsCreatesInvalidVersion() {
        assertFalse(new Version().isValid());
    }

    @Test
    public void testToStringWhenGivenInt() {
        assertEquals("123", new Version(123).toString());
    }

    @Test
    public void testIsValidWhenGivenInt() {
        assertTrue(new Version(123).isValid());
    }

    @Test
    public void testToStringWhenGivenIntAsString() {
        assertEquals("123", new Version("123").toString());
    }

    @Test
    public void testIsValidWhenGivenIntAsString() {
        assertTrue(new Version("123").isValid());
    }

    @Test
    public void testToStringWhenGivenVersionString() {
        assertEquals("0.1.2rc3", new Version("0.1.2rc3").toString());
    }

    @Test
    public void testIsValidWhenGivenVersionString() {
        assertTrue(new Version("0.1.2rc3").isValid());
    }

    @Test
    public void testIsInvalidWhenGivenOtherString() {
        assertFalse(new Version("Hello!").isValid());
    }

    @Test
    public void testIsInvalidWhenGivenInvalidGitHash() {
        assertFalse(new Version("0.1.3-12-gABCDEFG").isValid());
        assertFalse(new Version("0.1.3-12-g123").isValid());
        assertFalse(new Version("0.1.3-12-g123123123").isValid());
    }

    @Test
    public void testInvalidVersionsAreEqual() {
        assertEquals(0, new Version().compareTo(new Version()));
    }

    @Test
    public void testEqualNumericalVersionsAreEqual() {
        assertEquals(0, new Version(123).compareTo(new Version(123)));
    }

    @Test
    public void testEqualNumericalAndStringVersionsAreEqual() {
        assertEquals(0, new Version(123).compareTo(new Version("123")));
    }

    @Test
    public void testStringVersionsAreNewerThanNumericalVersions() {
        assertTrue(new Version(123).compareTo(new Version("0.1")) < 0);
    }

    @Test
    public void testNumericalVersionsAreOlderThanStringVersions() {
        assertTrue(new Version("0.1").compareTo(new Version(123)) > 0);
    }

    @Test
    public void testGreaterNumericalVersionIsNewer() {
        assertTrue(new Version(123).compareTo(new Version(124)) < 0);
    }

    @Test
    public void testLesserNumericalVersionIsOlder() {
        assertTrue(new Version(124).compareTo(new Version(123)) > 0);
    }

    @Test
    public void testEqualStringVersionsAreEqual() {
        assertEquals(0, new Version("1.2.3").compareTo(new Version("1.2.3")));
    }

    @Test
    public void testLongerStringVersionsAreNewer() {
        assertTrue(new Version("1.2.3").compareTo(new Version("1.2.3.1")) < 0);
    }

    @Test
    public void testHigherStringVersionsAreNewer() {
        assertTrue(new Version("1.2.3").compareTo(new Version("1.2.4")) < 0);
        assertTrue(new Version("1.2.3").compareTo(new Version("1.3.3")) < 0);
        assertTrue(new Version("1.2.3").compareTo(new Version("2.2.3")) < 0);
        assertTrue(new Version("1.2.3").compareTo(new Version("2.0")) < 0);
    }

    @Test
    public void testHigherStringVersionsAreNewerWithSuffixes() {
        assertTrue(new Version("1.2.3rc2").compareTo(new Version("1.2.4a1")) < 0);
        assertTrue(new Version("1.2.3b1").compareTo(new Version("1.3.3m8")) < 0);
        assertTrue(new Version("1.2.3m8").compareTo(new Version("2.2.3rc2")) < 0);
        assertTrue(new Version("1.2.3a6").compareTo(new Version("2.0b4")) < 0);
    }

    @Test
    public void testShorterStringVersionsAreOlder() {
        assertTrue(new Version("1.2.3.1").compareTo(new Version("1.2.3")) > 0);
    }

    @Test
    public void testLowerStringVersionsAreOlder() {
        assertTrue(new Version("1.2.4").compareTo(new Version("1.2.3")) > 0);
        assertTrue(new Version("1.3.3").compareTo(new Version("1.2.3")) > 0);
        assertTrue(new Version("2.2.3").compareTo(new Version("1.2.3")) > 0);
        assertTrue(new Version("2.0").compareTo(new Version("1.2.3")) > 0);
    }

    @Test
    public void testLowerStringVersionsAreOlderWithSuffixes() {
        assertTrue(new Version("1.2.4a1").compareTo(new Version("1.2.3rc2")) > 0);
        assertTrue(new Version("1.3.3m8").compareTo(new Version("1.2.3b1")) > 0);
        assertTrue(new Version("2.2.3rc2").compareTo(new Version("1.2.3m8")) > 0);
        assertTrue(new Version("2.0b4").compareTo(new Version("1.2.3a6")) > 0);
    }

    @Test
    public void testHigherGitSuffixesAreNewer() {
        assertTrue(new Version("1.2-17-gabcdeff").compareTo(new Version("1.2-18-gabcdeff")) < 0);
    }

    @Test
    public void testLowerGitSuffixesAreOlder() {
        assertTrue(new Version("1.2-18-gabcdeff").compareTo(new Version("1.2-17-gabcdeff")) > 0);
    }

    @Test
    public void testEqualVersionsWithDifferentGitHashesAreEqual() {
        assertEquals(0, new Version("1.2-17-g1234567").compareTo(new Version("1.2-17-gabcdeff")));
    }

    @Test
    public void testHigherSuffixesAreNewer() {
        assertTrue(new Version("1.2.3rc1").compareTo(new Version("1.2.3rc2")) < 0);
    }

    @Test
    public void testBetasAreNewerThanAlphas() {
        assertTrue(new Version("1.2.3a8").compareTo(new Version("1.2.3b5")) < 0);
    }

    @Test
    public void testAlphasAreNewerThanMilestones() {
        assertTrue(new Version("1.2.3m8").compareTo(new Version("1.2.3a1")) < 0);
    }

    @Test
    public void testReleaseCandidatesAreNewerThanBetas() {
        assertTrue(new Version("1.2.3b8").compareTo(new Version("1.2.3rc1")) < 0);
    }

    @Test
    public void testReleasesAreNewerThanReleaseCandidates() {
        assertTrue(new Version("1.2.3rc8").compareTo(new Version("1.2.3")) < 0);
    }

    @Test
    public void testHigherSuffixesAreNewerWithMilestones() {
        assertTrue(new Version("1.2.3m1rc1").compareTo(new Version("1.2.3m1rc2")) < 0);
    }

    @Test
    public void testBetasAreNewerThanAlphasWithMilestones() {
        assertTrue(new Version("1.2.3m1a8").compareTo(new Version("1.2.3m1b5")) < 0);
    }

    @Test
    public void testReleaseCandidatesAreNewerThanBetasWithMilestones() {
        assertTrue(new Version("1.2.3m1b8").compareTo(new Version("1.2.3m1rc1")) < 0);
    }

    @Test
    public void testReleasesAreNewerThanReleaseCandidatesWithMilestones() {
        assertTrue(new Version("1.2.3m1rc8").compareTo(new Version("1.2.3m1")) < 0);
    }

    @Test
    public void testLowerSuffixesAreOlder() {
        assertTrue(new Version("1.2.3rc2").compareTo(new Version("1.2.3rc1")) > 0);
    }

    @Test
    public void testAlphasAreOlderThanBetas() {
        assertTrue(new Version("1.2.3b5").compareTo(new Version("1.2.3a8")) > 0);
    }

    @Test
    public void testMilestonesAreOlderThanAlphas() {
        assertTrue(new Version("1.2.3a1").compareTo(new Version("1.2.3m8")) > 0);
    }

    @Test
    public void testBetasAreOlderThanReleaseCandidates() {
        assertTrue(new Version("1.2.3rc1").compareTo(new Version("1.2.3b8")) > 0);
    }

    @Test
    public void testReleaseCandidatesAreOlderThanReleases() {
        assertTrue(new Version("1.2.3").compareTo(new Version("1.2.3rc8")) > 0);
    }

    @Test
    public void testLowerSuffixesAreOlderWithMilestones() {
        assertTrue(new Version("1.2.3m1rc2").compareTo(new Version("1.2.3m1rc1")) > 0);
    }

    @Test
    public void testAlphasAreOlderThanBetasWithMilestones() {
        assertTrue(new Version("1.2.3m1b5").compareTo(new Version("1.2.3m1a8")) > 0);
    }

    @Test
    public void testBetasAreOlderThanReleaseCandidatesWithMilestones() {
        assertTrue(new Version("1.2.3m1rc1").compareTo(new Version("1.2.3m1b8")) > 0);
    }

    @Test
    public void testReleaseCandidatesAreOlderThanReleasesWithMilestones() {
        assertTrue(new Version("1.2.3m1").compareTo(new Version("1.2.3m1rc8")) > 0);
    }

    @Test
    public void testHashCodesEqualWhenIntVersionsEqual() {
        assertEquals(new Version(1).hashCode(), new Version(1).hashCode());
    }

    @Test
    public void testHashCodesEqualWhenStringVersionsEqual() {
        assertEquals(new Version("0.1").hashCode(), new Version("0.1").hashCode());
    }

}
