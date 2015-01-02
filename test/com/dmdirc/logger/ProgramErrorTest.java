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

import com.dmdirc.util.ClientInfo;

import java.util.Date;

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
    @Mock private ClientInfo clientInfo;

    @Test(expected = NullPointerException.class)
    public void testConstructorNullErrorLevel() {
        new ProgramError(null, "moo", null, null, new Date(), clientInfo, errorManager);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullMessage() {
        new ProgramError(ErrorLevel.HIGH, null, null, null, new Date(), clientInfo, errorManager);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyMessage() {
        new ProgramError(ErrorLevel.HIGH, "", null, null, new Date(), clientInfo, errorManager);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullDate() {
        new ProgramError(ErrorLevel.HIGH, "moo", null, null, null, clientInfo, errorManager);
    }

    @Test
    public void testConstructorGood() {
        new ProgramError(ErrorLevel.HIGH, "moo", new UnsupportedOperationException(),
                null, new Date(), clientInfo, errorManager);
    }

    @Test
    public void testGetLevel() {
        final ProgramError pe = new ProgramError(ErrorLevel.HIGH, "moo",
                new UnsupportedOperationException(), null, new Date(), clientInfo, errorManager);
        assertEquals(ErrorLevel.HIGH, pe.getLevel());
    }

    @Test
    public void testGetMessage() {
        final ProgramError pe = new ProgramError(ErrorLevel.HIGH, "moo",
                new UnsupportedOperationException(), null, new Date(), clientInfo, errorManager);
        assertEquals("moo", pe.getMessage());
    }

    @Test
    public void testGetDate() {
        final Date date = new Date();
        final ProgramError pe = new ProgramError(ErrorLevel.HIGH, "moo",
                new UnsupportedOperationException(), null, date, clientInfo, errorManager);
        assertEquals(date, pe.getDate());
    }

    @Test
    public void testReportStatus() {
        final ProgramError pe = new ProgramError(ErrorLevel.HIGH, "moo",
                new UnsupportedOperationException(), null, new Date(), clientInfo, errorManager);
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
        final ProgramError pe = new ProgramError(ErrorLevel.HIGH, "moo",
                new UnsupportedOperationException(), null, new Date(), clientInfo, errorManager);
        assertTrue(pe.toString().contains("moo"));
    }

    @Test
    public void testEquals() {
        final Exception ex = new UnsupportedOperationException();
        final ProgramError pe1 = new ProgramError(ErrorLevel.LOW, "moo",
                ex, null, new Date(), clientInfo, errorManager);
        final ProgramError pe2 = new ProgramError(ErrorLevel.LOW, "moo",
                ex, null, new Date(), clientInfo, errorManager);
        final ProgramError pe3 = new ProgramError(ErrorLevel.MEDIUM, "moo",
                ex, null, new Date(), clientInfo, errorManager);
        final ProgramError pe4 = new ProgramError(ErrorLevel.LOW, "bar",
                ex, null, new Date(), clientInfo, errorManager);
        final ProgramError pe5 = new ProgramError(ErrorLevel.LOW, "moo",
                null, "Hello", new Date(), clientInfo, errorManager);

        assertFalse(pe1.equals(null)); // NOPMD
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
