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

import static junit.framework.TestCase.assertEquals;
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
        assertEquals(ErrorLevel.MEDIUM, ErrorLevel.LOW.getMoreImportant(ErrorLevel.MEDIUM));
        assertEquals(ErrorLevel.HIGH, ErrorLevel.LOW.getMoreImportant(ErrorLevel.HIGH));
        assertEquals(ErrorLevel.FATAL, ErrorLevel.LOW.getMoreImportant(ErrorLevel.FATAL));
        assertEquals(ErrorLevel.LOW, ErrorLevel.LOW.getMoreImportant(ErrorLevel.LOW));
        assertEquals(ErrorLevel.LOW, ErrorLevel.LOW.getMoreImportant(ErrorLevel.UNKNOWN));
        assertEquals(ErrorLevel.LOW, ErrorLevel.LOW.getMoreImportant(null));
    }

    @Test
    public void testMoreImportantMedium() {
        assertEquals(ErrorLevel.MEDIUM, ErrorLevel.MEDIUM.getMoreImportant(ErrorLevel.MEDIUM));
        assertEquals(ErrorLevel.HIGH, ErrorLevel.MEDIUM.getMoreImportant(ErrorLevel.HIGH));
        assertEquals(ErrorLevel.FATAL, ErrorLevel.MEDIUM.getMoreImportant(ErrorLevel.FATAL));
        assertEquals(ErrorLevel.MEDIUM, ErrorLevel.MEDIUM.getMoreImportant(ErrorLevel.LOW));
        assertEquals(ErrorLevel.MEDIUM, ErrorLevel.MEDIUM.getMoreImportant(ErrorLevel.UNKNOWN));
        assertEquals(ErrorLevel.MEDIUM, ErrorLevel.MEDIUM.getMoreImportant(null));
    }

    @Test
    public void testMoreImportantHigh() {
        assertEquals(ErrorLevel.HIGH, ErrorLevel.HIGH.getMoreImportant(ErrorLevel.MEDIUM));
        assertEquals(ErrorLevel.HIGH, ErrorLevel.HIGH.getMoreImportant(ErrorLevel.HIGH));
        assertEquals(ErrorLevel.FATAL, ErrorLevel.HIGH.getMoreImportant(ErrorLevel.FATAL));
        assertEquals(ErrorLevel.HIGH, ErrorLevel.HIGH.getMoreImportant(ErrorLevel.LOW));
        assertEquals(ErrorLevel.HIGH, ErrorLevel.HIGH.getMoreImportant(ErrorLevel.UNKNOWN));
        assertEquals(ErrorLevel.HIGH, ErrorLevel.HIGH.getMoreImportant(null));
    }

    @Test
    public void testMoreImportantFatal() {
        assertEquals(ErrorLevel.FATAL, ErrorLevel.FATAL.getMoreImportant(ErrorLevel.MEDIUM));
        assertEquals(ErrorLevel.FATAL, ErrorLevel.FATAL.getMoreImportant(ErrorLevel.HIGH));
        assertEquals(ErrorLevel.FATAL, ErrorLevel.FATAL.getMoreImportant(ErrorLevel.FATAL));
        assertEquals(ErrorLevel.FATAL, ErrorLevel.FATAL.getMoreImportant(ErrorLevel.LOW));
        assertEquals(ErrorLevel.FATAL, ErrorLevel.FATAL.getMoreImportant(ErrorLevel.UNKNOWN));
        assertEquals(ErrorLevel.FATAL, ErrorLevel.FATAL.getMoreImportant(null));
    }

    @Test
    public void testMoreImportantUnknown() {
        assertEquals(ErrorLevel.MEDIUM, ErrorLevel.UNKNOWN.getMoreImportant(ErrorLevel.MEDIUM));
        assertEquals(ErrorLevel.HIGH, ErrorLevel.UNKNOWN.getMoreImportant(ErrorLevel.HIGH));
        assertEquals(ErrorLevel.FATAL, ErrorLevel.UNKNOWN.getMoreImportant(ErrorLevel.FATAL));
        assertEquals(ErrorLevel.LOW, ErrorLevel.UNKNOWN.getMoreImportant(ErrorLevel.LOW));
        assertEquals(ErrorLevel.UNKNOWN, ErrorLevel.UNKNOWN.getMoreImportant(ErrorLevel.UNKNOWN));
        assertEquals(ErrorLevel.UNKNOWN, ErrorLevel.UNKNOWN.getMoreImportant(null));
    }

}
