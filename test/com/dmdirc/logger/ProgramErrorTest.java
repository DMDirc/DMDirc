/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ProgramErrorTest {

    @Mock private ErrorManager errorManager;

    @Before
    public void setup() {
        ErrorManager.setErrorManager(errorManager);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeID() {
        new ProgramError(-1, ErrorLevel.HIGH, "moo", null, null, new Date());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullErrorLevel() {
        new ProgramError(1, null, "moo", null, null, new Date());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullMessage() {
        new ProgramError(1, ErrorLevel.HIGH, null, null, null, new Date());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyMessage() {
        new ProgramError(1, ErrorLevel.HIGH, "", null, null, new Date());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullDate() {
        new ProgramError(1, ErrorLevel.HIGH, "moo", null, null, null);
    }

    @Test
    public void testConstructorGood() {
        new ProgramError(1, ErrorLevel.HIGH, "moo", new Exception(), null, new Date());
    }

    @Test
    public void testGetLevel() {
        final ProgramError pe = new ProgramError(1, ErrorLevel.HIGH, "moo",
                new Exception(), null, new Date());
        assertEquals(ErrorLevel.HIGH, pe.getLevel());
    }

    @Test
    public void testGetMessage() {
        final ProgramError pe = new ProgramError(1, ErrorLevel.HIGH, "moo",
                new Exception(), null, new Date());
        assertEquals("moo", pe.getMessage());
    }

    @Test
    public void testGetDate() {
        final Date date = new Date();
        final ProgramError pe = new ProgramError(1, ErrorLevel.HIGH, "moo",
                new Exception(), null, date);
        assertEquals(date, pe.getDate());
    }

    @Test
    public void testReportStatus() {
        final ProgramError pe = new ProgramError(1, ErrorLevel.HIGH, "moo",
                new Exception(), null, new Date());
        assertEquals(ErrorReportStatus.WAITING, pe.getReportStatus());
        pe.setReportStatus(null);
        assertEquals(ErrorReportStatus.WAITING, pe.getReportStatus());
        pe.setReportStatus(ErrorReportStatus.WAITING);
        assertEquals(ErrorReportStatus.WAITING, pe.getReportStatus());
        pe.setReportStatus(ErrorReportStatus.ERROR);
        assertEquals(ErrorReportStatus.ERROR, pe.getReportStatus());
    }

    @Test
    public void testToString() {
        final ProgramError pe = new ProgramError(1, ErrorLevel.HIGH, "moo",
                new Exception(), null, new Date());
        assertTrue(pe.toString().indexOf("moo") > -1);
    }

    @Test
    public void testEquals() {
        final Exception ex = new Exception();
        final ProgramError pe1 = new ProgramError(10, ErrorLevel.LOW, "moo",
                ex, null, new Date());
        final ProgramError pe2 = new ProgramError(11, ErrorLevel.LOW, "moo",
                ex, null, new Date());
        final ProgramError pe3 = new ProgramError(10, ErrorLevel.MEDIUM, "moo",
                ex, null, new Date());
        final ProgramError pe4 = new ProgramError(10, ErrorLevel.LOW, "bar",
                ex, null, new Date());
        final ProgramError pe5 = new ProgramError(10, ErrorLevel.LOW, "moo",
                null, "Hello", new Date());

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
