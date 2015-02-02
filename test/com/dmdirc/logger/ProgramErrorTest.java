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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.ProgramErrorStatusEvent;
import com.dmdirc.util.ClientInfo;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ProgramErrorTest {

    @Mock private ErrorManager errorManager;
    @Mock private DMDircMBassador eventBus;
    @Mock private ClientInfo clientInfo;
    @Captor private ArgumentCaptor<ProgramErrorStatusEvent> event;

    @Test(expected = NullPointerException.class)
    public void testConstructorNullErrorLevel() {
        new ProgramError(null, "moo", null, new Date(),eventBus, true);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullDate() {
        new ProgramError(ErrorLevel.HIGH, "moo", null, null, eventBus, true);
    }

    @Test
    public void testConstructorGood() {
        new ProgramError(ErrorLevel.HIGH, "moo", new UnsupportedOperationException(),
                new Date(), eventBus, true);
    }

    @Test
    public void testGetLevel() {
        final ProgramError pe = new ProgramError(ErrorLevel.HIGH, "moo",
                new UnsupportedOperationException(), new Date(), eventBus, true);
        assertEquals(ErrorLevel.HIGH, pe.getLevel());
    }

    @Test
    public void testGetMessage() {
        final ProgramError pe = new ProgramError(ErrorLevel.HIGH, "moo",
                new UnsupportedOperationException(), new Date(), eventBus, true);
        assertEquals("moo", pe.getMessage());
    }

    @Test
    public void testGetDate() {
        final Date date = new Date();
        final ProgramError pe = new ProgramError(ErrorLevel.HIGH, "moo",
                new UnsupportedOperationException(), date, eventBus, true);
        assertEquals(date, pe.getDate());
    }

    @Test
    public void testIsAppError() {
        final ProgramError pe = new ProgramError(ErrorLevel.HIGH, "moo",
                new UnsupportedOperationException(), new Date(), eventBus, true);
        assertTrue(pe.isAppError());
    }

    @Test
    public void testReportStatus() {
        final ProgramError pe = new ProgramError(ErrorLevel.HIGH, "moo",
                new UnsupportedOperationException(), new Date(), eventBus, true);
        assertEquals(ErrorReportStatus.WAITING, pe.getReportStatus());
        pe.setReportStatus(null);
        assertEquals(ErrorReportStatus.WAITING, pe.getReportStatus());
        verify(eventBus, never()).publishAsync(event.capture());
        pe.setReportStatus(ErrorReportStatus.WAITING);
        assertEquals(ErrorReportStatus.WAITING, pe.getReportStatus());
        verify(eventBus, never()).publishAsync(event.capture());
        pe.setReportStatus(ErrorReportStatus.ERROR);
        assertEquals(ErrorReportStatus.ERROR, pe.getReportStatus());
        verify(eventBus).publishAsync(event.capture());
        assertEquals(pe, event.getValue().getError());
    }

    @Test
    public void testToString() {
        final ProgramError pe = new ProgramError(ErrorLevel.HIGH, "moo",
                new UnsupportedOperationException(), new Date(), eventBus, true);
        assertTrue(pe.toString().contains("moo"));
    }

    @Test
    public void testEquals() {
        final Exception ex = new UnsupportedOperationException();
        final ProgramError pe1 = new ProgramError(ErrorLevel.LOW, "moo",
                ex, new Date(), eventBus, true);
        final ProgramError pe2 = new ProgramError(ErrorLevel.LOW, "moo",
                ex, new Date(), eventBus, true);
        final ProgramError pe3 = new ProgramError(ErrorLevel.MEDIUM, "moo",
                ex, new Date(), eventBus, true);
        final ProgramError pe4 = new ProgramError(ErrorLevel.LOW, "bar",
                ex, new Date(), eventBus, true);
        final ProgramError pe5 = new ProgramError(ErrorLevel.LOW, "moo",
                null, new Date(), eventBus, true);

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
