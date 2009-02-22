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
package com.dmdirc.logger;

import java.util.Arrays;
import java.util.Date;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProgramErrorTest {

    @Test
    public void testConstructorNegativeID() {
        boolean exception = false;

        try {
            new ProgramError(-1, ErrorLevel.HIGH, "moo", new String[0], new Date());
        } catch (Exception ex) {
            exception = true;
        }

        assertTrue(exception);
    }

    @Test
    public void testConstructorNullErrorLevel() {
        boolean exception = false;

        try {
            new ProgramError(1, null, "moo", new String[0], new Date());
        } catch (Exception ex) {
            exception = true;
        }

        assertTrue(exception);
    }

    @Test
    public void testConstructorNullMessage() {
        boolean exception = false;

        try {
            new ProgramError(1, ErrorLevel.HIGH, null, new String[0], new Date());
        } catch (Exception ex) {
            exception = true;
        }

        assertTrue(exception);
    }

    @Test
    public void testConstructorEmptyMessage() {
        boolean exception = false;

        try {
            new ProgramError(1, ErrorLevel.HIGH, "", new String[0], new Date());
        } catch (Exception ex) {
            exception = true;
        }

        assertTrue(exception);
    }

    @Test
    public void testConstructorNullTrace() {
        boolean exception = false;

        try {
            new ProgramError(1, ErrorLevel.HIGH, "moo", null, new Date());
        } catch (Exception ex) {
            exception = true;
        }

        assertTrue(exception);
    }

    @Test
    public void testConstructorNullDate() {
        boolean exception = false;

        try {
            new ProgramError(1, ErrorLevel.HIGH, "moo", new String[0], null);
        } catch (Exception ex) {
            exception = true;
        }

        assertTrue(exception);
    }

    @Test
    public void testConstructorGood() {
        boolean exception = false;

        try {
            new ProgramError(1, ErrorLevel.HIGH, "moo", new String[0], new Date());
        } catch (IllegalArgumentException ex) {
            exception = true;
        }

        assertFalse(exception);
    }

    @Test
    public void testGetLevel() {
        final ProgramError pe = new ProgramError(1, ErrorLevel.HIGH, "moo",
                new String[0], new Date());
        assertEquals(ErrorLevel.HIGH, pe.getLevel());
    }

    @Test
    public void testGetMessage() {
        final ProgramError pe = new ProgramError(1, ErrorLevel.HIGH, "moo",
                new String[0], new Date());
        assertEquals("moo", pe.getMessage());
    }

    @Test
    public void testGetTrace() {
        final String[] trace = new String[]{"abc", "def", "ghi", ""};
        final ProgramError pe = new ProgramError(1, ErrorLevel.HIGH, "moo",
                trace, new Date());
        assertTrue(Arrays.equals(trace, pe.getTrace()));
    }

    @Test
    public void testGetDate() {
        final Date date = new Date();
        final ProgramError pe = new ProgramError(1, ErrorLevel.HIGH, "moo",
                new String[0], date);
        assertEquals(date, pe.getDate());
    }

    @Test
    public void testReportStatus() {
        final ProgramError pe = new ProgramError(1, ErrorLevel.HIGH, "moo",
                new String[0], new Date());
        assertEquals(ErrorReportStatus.WAITING, pe.getReportStatus());
        pe.setReportStatus(null);
        assertEquals(ErrorReportStatus.WAITING, pe.getReportStatus());
        pe.setReportStatus(ErrorReportStatus.WAITING);
        assertEquals(ErrorReportStatus.WAITING, pe.getReportStatus());
        pe.setReportStatus(ErrorReportStatus.ERROR);
        assertEquals(ErrorReportStatus.ERROR, pe.getReportStatus());
    }
    
    @Test
    public void testFixedStatus() {
        final ProgramError pe = new ProgramError(1, ErrorLevel.HIGH, "moo",
                new String[0], new Date());
        assertEquals(ErrorFixedStatus.UNKNOWN, pe.getFixedStatus());
        pe.setFixedStatus(null);
        assertEquals(ErrorFixedStatus.UNKNOWN, pe.getFixedStatus());
        pe.setFixedStatus(ErrorFixedStatus.UNKNOWN);
        assertEquals(ErrorFixedStatus.UNKNOWN, pe.getFixedStatus());
        pe.setFixedStatus(ErrorFixedStatus.INVALID);
        assertEquals(ErrorFixedStatus.INVALID, pe.getFixedStatus());
    }
    
    @Test
    public void testToString() {
        final ProgramError pe = new ProgramError(1, ErrorLevel.HIGH, "moo",
                new String[0], new Date());
        assertTrue(pe.toString().indexOf("moo") > -1);
    }
    
    @Test
    public void testEquals() {
        final ProgramError pe1 = new ProgramError(10, ErrorLevel.LOW, "moo",
                new String[0], new Date());
        final ProgramError pe2 = new ProgramError(11, ErrorLevel.LOW, "moo",
                new String[0], new Date());
        final ProgramError pe3 = new ProgramError(10, ErrorLevel.MEDIUM, "moo",
                new String[0], new Date());
        final ProgramError pe4 = new ProgramError(10, ErrorLevel.LOW, "bar",
                new String[0], new Date());
        final ProgramError pe5 = new ProgramError(10, ErrorLevel.LOW, "moo",
                new String[]{"Hello"}, new Date());
        
        assertFalse(pe1.equals(null));
        assertFalse(pe1.equals("moo"));
        
        assertTrue(pe1.equals(pe2));
        assertTrue(pe1.equals(pe1));
        assertTrue(pe2.equals(pe1));
        
        assertFalse(pe1.equals(pe3));
        assertFalse(pe1.equals(pe4));
        assertFalse(pe1.equals(pe5));
        assertFalse(pe4.equals(pe5));
        assertFalse(pe4.equals(pe3));
        
        assertEquals(pe1.hashCode(), pe2.hashCode());
        assertEquals(pe1.hashCode(), pe1.hashCode());
    }

}