/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class VersionTest {

    private final Version v1, v2;
    private final int min, max;

    public VersionTest(Version v1, Version v2, int max, int min) {
        this.v1 = v1;
        this.v2 = v2;
        this.min = min;
        this.max = max;
    }

    @Test
    public void testData() {
        assertTrue(max + " > " + v1 + ".compareTo(" + v2 + ")", max > v1.compareTo(v2));
        assertTrue(min + " < " + v1 + ".compareTo(" + v2 + ")", min < v1.compareTo(v2));
    }

    @Parameterized.Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {new Version(1), new Version(2), 0, Integer.MIN_VALUE},
            {new Version(1), new Version(2000), 0, Integer.MIN_VALUE},
            {new Version(2), new Version(1), Integer.MAX_VALUE, 0},
            {new Version(1), new Version(1), 1, -1},
            {new Version(0), new Version(0), 1, -1},
            {new Version(1), new Version("0.6"), 0, Integer.MIN_VALUE},
            {new Version("0.6"), new Version("0.6"), 1, -1},
            {new Version("0.6"), new Version("0.6.3"), 0, Integer.MIN_VALUE},
            {new Version("0.6-149-gaaaaaaa"), new Version("0.6.3"), 0, Integer.MIN_VALUE},
            {new Version("0.6-149-gaaaaaaa"), new Version("0.6.3-149-gaaaaaaa"), 0, Integer.MIN_VALUE},
            {new Version("0.6.3"), new Version("0.6.3-149-gaaaaaaa"), 0, Integer.MIN_VALUE},
            {new Version("0.6.3a1"), new Version("0.6.3"), 0, Integer.MIN_VALUE},
            {new Version("0.6.3b1"), new Version("0.6.3"), 0, Integer.MIN_VALUE},
            {new Version("0.6.3rc1"), new Version("0.6.3"), 0, Integer.MIN_VALUE},
            {new Version("0.6.3m1"), new Version("0.6.3"), 0, Integer.MIN_VALUE},
            {new Version("0.6.3m1b1"), new Version("0.6.3"), 0, Integer.MIN_VALUE},
            {new Version("0.6.3m1a1"), new Version("0.6.3m1b1"), 0, Integer.MIN_VALUE},
            {new Version("0.6.3m1a1"), new Version("0.6.3m1a1-149-gaaaaaaa"), 0, Integer.MIN_VALUE},
            {new Version("0.6.3-148-gaaaaaaa"), new Version("0.6.3-149-gaaaaaaa"), 0, Integer.MIN_VALUE},
            {new Version("0.6.3-148-gaaaaaaa"), new Version("0.6.3-148-gaaaaaaa"), 1, -1},
        });
    }

}