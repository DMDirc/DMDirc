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
package com.dmdirc.logger;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ErrorLevelTest {

    @Test
    public void testToString() {
        assertTrue("Low".equalsIgnoreCase(ErrorLevel.LOW.toString()));
        assertTrue("Medium".equalsIgnoreCase(ErrorLevel.MEDIUM.toString()));
        assertTrue("High".equalsIgnoreCase(ErrorLevel.HIGH.toString()));
        assertTrue("Fatal".equalsIgnoreCase(ErrorLevel.FATAL.toString()));
        assertTrue("Unknown".equalsIgnoreCase(ErrorLevel.UNKNOWN.toString()));
    }

    @Test
    public void testIcons() {
        assertTrue("info".equalsIgnoreCase(ErrorLevel.LOW.getIcon()));
        assertTrue("warning".equalsIgnoreCase(ErrorLevel.MEDIUM.getIcon()));
        assertTrue("error".equalsIgnoreCase(ErrorLevel.HIGH.getIcon()));
        assertTrue("error".equalsIgnoreCase(ErrorLevel.FATAL.getIcon()));
        assertTrue("info".equalsIgnoreCase(ErrorLevel.UNKNOWN.getIcon()));
    }

    @Test
    public void testMoreImportantLow() {
        assertTrue(ErrorLevel.LOW.moreImportant(ErrorLevel.MEDIUM));
        assertTrue(ErrorLevel.LOW.moreImportant(ErrorLevel.HIGH));
        assertTrue(ErrorLevel.LOW.moreImportant(ErrorLevel.FATAL));
        assertFalse(ErrorLevel.LOW.moreImportant(ErrorLevel.LOW));
        assertFalse(ErrorLevel.LOW.moreImportant(ErrorLevel.UNKNOWN));
        assertFalse(ErrorLevel.LOW.moreImportant(null));
    }

    @Test
    public void testMoreImportantMedium() {
        assertFalse(ErrorLevel.MEDIUM.moreImportant(ErrorLevel.MEDIUM));
        assertTrue(ErrorLevel.MEDIUM.moreImportant(ErrorLevel.HIGH));
        assertTrue(ErrorLevel.MEDIUM.moreImportant(ErrorLevel.FATAL));
        assertFalse(ErrorLevel.MEDIUM.moreImportant(ErrorLevel.LOW));
        assertFalse(ErrorLevel.MEDIUM.moreImportant(ErrorLevel.UNKNOWN));
        assertFalse(ErrorLevel.MEDIUM.moreImportant(null));
    }

    @Test
    public void testMoreImportantHigh() {
        assertFalse(ErrorLevel.HIGH.moreImportant(ErrorLevel.MEDIUM));
        assertFalse(ErrorLevel.HIGH.moreImportant(ErrorLevel.HIGH));
        assertTrue(ErrorLevel.HIGH.moreImportant(ErrorLevel.FATAL));
        assertFalse(ErrorLevel.HIGH.moreImportant(ErrorLevel.LOW));
        assertFalse(ErrorLevel.HIGH.moreImportant(ErrorLevel.UNKNOWN));
        assertFalse(ErrorLevel.HIGH.moreImportant(null));
    }

    @Test
    public void testMoreImportantFatal() {
        assertFalse(ErrorLevel.FATAL.moreImportant(ErrorLevel.MEDIUM));
        assertFalse(ErrorLevel.FATAL.moreImportant(ErrorLevel.HIGH));
        assertFalse(ErrorLevel.FATAL.moreImportant(ErrorLevel.FATAL));
        assertFalse(ErrorLevel.FATAL.moreImportant(ErrorLevel.LOW));
        assertFalse(ErrorLevel.FATAL.moreImportant(ErrorLevel.UNKNOWN));
        assertFalse(ErrorLevel.FATAL.moreImportant(null));
    }

    @Test
    public void testMoreImportantUnknown() {
        assertTrue(ErrorLevel.UNKNOWN.moreImportant(ErrorLevel.MEDIUM));
        assertTrue(ErrorLevel.UNKNOWN.moreImportant(ErrorLevel.HIGH));
        assertTrue(ErrorLevel.UNKNOWN.moreImportant(ErrorLevel.FATAL));
        assertTrue(ErrorLevel.UNKNOWN.moreImportant(ErrorLevel.LOW));
        assertFalse(ErrorLevel.UNKNOWN.moreImportant(ErrorLevel.UNKNOWN));
        assertFalse(ErrorLevel.UNKNOWN.moreImportant(null));
    }

}
