/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.actions;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class StringComponentsTest {

    private final String string1;
    private final String string2;

    public StringComponentsTest(final String string1, final String string2) {
        this.string1 = string1;
        this.string2 = string2;
    }

    @Test
    public void testEquals() {
        assertEquals(string1.equalsIgnoreCase(string2),
                CoreActionComparison.STRING_EQUALS.test(string1, string2));
    }

    @Test
    public void testNotEquals() {
        assertEquals(!string1.equalsIgnoreCase(string2),
                CoreActionComparison.STRING_NEQUALS.test(string1, string2));
    }

    @Test
    public void testStartsWith() {
        assertEquals(string1.startsWith(string2),
                CoreActionComparison.STRING_STARTSWITH.test(string1, string2));
    }

    @Test
    public void testContains() {
        assertEquals(string1.contains(string2),
                CoreActionComparison.STRING_CONTAINS.test(string1, string2));
    }

    @Test
    public void testNotContains() {
        assertEquals(!string1.contains(string2),
                CoreActionComparison.STRING_NCONTAINS.test(string1, string2));
    }

    @Parameterized.Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"foo", "foo"},
            {"FOO", "foo"},
            {"", "foo"},
            {"foo", ""},
            {"abc foo def", "foo"}
        });
    }

}
