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

import org.junit.Test;

import static org.junit.Assert.*;

public class CoreActionComparisonTest {

    @Test
    public void testStringRegex() {
        assertEquals(String.class, CoreActionComparison.STRING_REGEX.appliesTo());
        assertTrue(CoreActionComparison.STRING_REGEX.getName().toLowerCase().contains("reg"));
        assertTrue(CoreActionComparison.STRING_REGEX.test("hello", "h.*?o"));
        assertFalse(CoreActionComparison.STRING_REGEX.test("hello", "h.{8}o"));
        assertFalse(CoreActionComparison.STRING_REGEX.test("hello", "?!!?!{}"));
    }

    @Test
    public void testStringEquals() {
        assertEquals(String.class, CoreActionComparison.STRING_EQUALS.appliesTo());
        assertTrue(CoreActionComparison.STRING_EQUALS.getName().toLowerCase().contains("equal"));
        assertTrue(CoreActionComparison.STRING_EQUALS.test("hello", "hello"));
        assertTrue(CoreActionComparison.STRING_EQUALS.test("hello", "HELLO"));
        assertFalse(CoreActionComparison.STRING_EQUALS.test("hello", "h.{8}o"));
        assertFalse(CoreActionComparison.STRING_EQUALS.test("hello", "?!!?!{}"));
    }

    @Test
    public void testStringNEquals() {
        assertEquals(String.class, CoreActionComparison.STRING_NEQUALS.appliesTo());
        assertTrue(CoreActionComparison.STRING_NEQUALS.getName().toLowerCase().contains("equal"));
        assertTrue(CoreActionComparison.STRING_NEQUALS.getName().toLowerCase().contains("not")
                || CoreActionComparison.STRING_NEQUALS.getName().toLowerCase().contains("n't"));
        assertFalse(CoreActionComparison.STRING_NEQUALS.test("hello", "hello"));
        assertFalse(CoreActionComparison.STRING_NEQUALS.test("hello", "HELLO"));
        assertTrue(CoreActionComparison.STRING_NEQUALS.test("hello", "h.{8}o"));
        assertTrue(CoreActionComparison.STRING_NEQUALS.test("hello", "?!!?!{}"));
    }

    @Test
    public void testStringStartsWith() {
        assertEquals(String.class, CoreActionComparison.STRING_STARTSWITH.appliesTo());
        assertTrue(CoreActionComparison.STRING_STARTSWITH.getName().toLowerCase().contains("start"));
        assertTrue(CoreActionComparison.STRING_STARTSWITH.test("hello", "hello"));
        assertTrue(CoreActionComparison.STRING_STARTSWITH.test("hello", "he"));
        assertFalse(CoreActionComparison.STRING_STARTSWITH.test("hello", "h.{8}o"));
        assertFalse(CoreActionComparison.STRING_STARTSWITH.test("hello", "?!!?!{}"));
    }

    @Test
    public void testStringContains() {
        assertEquals(String.class, CoreActionComparison.STRING_CONTAINS.appliesTo());
        assertTrue(CoreActionComparison.STRING_CONTAINS.getName().toLowerCase().contains("contain"));
        assertTrue(CoreActionComparison.STRING_CONTAINS.test("hello", "hello"));
        assertTrue(CoreActionComparison.STRING_CONTAINS.test("hello", "lo"));
        assertFalse(CoreActionComparison.STRING_CONTAINS.test("hello", "h.{8}o"));
        assertFalse(CoreActionComparison.STRING_CONTAINS.test("hello", "?!!?!{}"));
    }

    @Test
    public void testStringNContains() {
        assertEquals(String.class, CoreActionComparison.STRING_NCONTAINS.appliesTo());
        assertTrue(
                CoreActionComparison.STRING_NCONTAINS.getName().toLowerCase().contains("contain"));
        assertTrue(CoreActionComparison.STRING_NCONTAINS.getName().toLowerCase().contains("not")
                || CoreActionComparison.STRING_NCONTAINS.getName().toLowerCase().contains("n't"));
        assertFalse(CoreActionComparison.STRING_NCONTAINS.test("hello", "hello"));
        assertFalse(CoreActionComparison.STRING_NCONTAINS.test("hello", "lo"));
        assertTrue(CoreActionComparison.STRING_NCONTAINS.test("hello", "h.{8}o"));
        assertTrue(CoreActionComparison.STRING_NCONTAINS.test("hello", "?!!?!{}"));
    }

    @Test
    public void testBoolIs() {
        assertEquals(Boolean.class, CoreActionComparison.BOOL_IS.appliesTo());
        assertTrue(CoreActionComparison.BOOL_IS.getName().toLowerCase().contains("is"));
        assertTrue(CoreActionComparison.BOOL_IS.test(Boolean.TRUE, "true"));
        assertTrue(CoreActionComparison.BOOL_IS.test(Boolean.FALSE, "false"));
        assertFalse(CoreActionComparison.BOOL_IS.test(Boolean.FALSE, "true"));
        assertFalse(CoreActionComparison.BOOL_IS.test(Boolean.TRUE, "false"));
    }

}
