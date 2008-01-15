/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import javax.swing.Icon;
import org.junit.Test;
import static org.junit.Assert.*;

public class ErrorLevelTest extends junit.framework.TestCase {

    @Test
    public void testToString() {
        assertTrue("Low".equalsIgnoreCase(ErrorLevel.LOW.toString()));
        assertTrue("Medium".equalsIgnoreCase(ErrorLevel.MEDIUM.toString()));
        assertTrue("High".equalsIgnoreCase(ErrorLevel.HIGH.toString()));
        assertTrue("Fatal".equalsIgnoreCase(ErrorLevel.FATAL.toString()));
        assertTrue("Unknown".equalsIgnoreCase(ErrorLevel.UNKNOWN.toString()));
    }

    @Test
    public void testGetIcon() {
        assertTrue(ErrorLevel.LOW.getIcon() instanceof Icon);
        assertTrue(ErrorLevel.MEDIUM.getIcon() instanceof Icon);
        assertTrue(ErrorLevel.HIGH.getIcon() instanceof Icon);
        assertTrue(ErrorLevel.FATAL.getIcon() instanceof Icon);
        assertTrue(ErrorLevel.UNKNOWN.getIcon() instanceof Icon);
    }

    @Test
    public void testMoreImportantLow() {
        assertTrue(ErrorLevel.LOW.moreImportant(ErrorLevel.MEDIUM));
        assertTrue(ErrorLevel.LOW.moreImportant(ErrorLevel.HIGH));
        assertTrue(ErrorLevel.LOW.moreImportant(ErrorLevel.FATAL));
        assertFalse(ErrorLevel.LOW.moreImportant(ErrorLevel.LOW));
        assertFalse(ErrorLevel.LOW.moreImportant(ErrorLevel.UNKNOWN));
    }

    @Test
    public void testMoreImportantMedium() {
        assertFalse(ErrorLevel.MEDIUM.moreImportant(ErrorLevel.MEDIUM));
        assertTrue(ErrorLevel.MEDIUM.moreImportant(ErrorLevel.HIGH));
        assertTrue(ErrorLevel.MEDIUM.moreImportant(ErrorLevel.FATAL));
        assertFalse(ErrorLevel.MEDIUM.moreImportant(ErrorLevel.LOW));
        assertFalse(ErrorLevel.MEDIUM.moreImportant(ErrorLevel.UNKNOWN));
    }

    @Test
    public void testMoreImportantHigh() {
        assertFalse(ErrorLevel.HIGH.moreImportant(ErrorLevel.MEDIUM));
        assertFalse(ErrorLevel.HIGH.moreImportant(ErrorLevel.HIGH));
        assertTrue(ErrorLevel.HIGH.moreImportant(ErrorLevel.FATAL));
        assertFalse(ErrorLevel.HIGH.moreImportant(ErrorLevel.LOW));
        assertFalse(ErrorLevel.HIGH.moreImportant(ErrorLevel.UNKNOWN));
    }

    @Test
    public void testMoreImportantFatal() {
        assertFalse(ErrorLevel.FATAL.moreImportant(ErrorLevel.MEDIUM));
        assertFalse(ErrorLevel.FATAL.moreImportant(ErrorLevel.HIGH));
        assertFalse(ErrorLevel.FATAL.moreImportant(ErrorLevel.FATAL));
        assertFalse(ErrorLevel.FATAL.moreImportant(ErrorLevel.LOW));
        assertFalse(ErrorLevel.FATAL.moreImportant(ErrorLevel.UNKNOWN));
    }
    
    @Test
    public void testMoreImportantUnknown() {
        assertTrue(ErrorLevel.UNKNOWN.moreImportant(ErrorLevel.MEDIUM));
        assertTrue(ErrorLevel.UNKNOWN.moreImportant(ErrorLevel.HIGH));
        assertTrue(ErrorLevel.UNKNOWN.moreImportant(ErrorLevel.FATAL));
        assertTrue(ErrorLevel.UNKNOWN.moreImportant(ErrorLevel.LOW));
        assertFalse(ErrorLevel.UNKNOWN.moreImportant(ErrorLevel.UNKNOWN));
    }    

}