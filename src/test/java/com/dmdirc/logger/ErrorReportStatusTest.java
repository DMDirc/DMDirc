/*
 * Copyright (c) 2006-2017 DMDirc Developers
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ErrorReportStatusTest {

    @Test
    public void testToString() {
        assertEquals("Queued", ErrorReportStatus.QUEUED.toString());
        assertEquals("Error sending", ErrorReportStatus.ERROR.toString());
        assertEquals("Waiting", ErrorReportStatus.WAITING.toString());
        assertEquals("Sending...", ErrorReportStatus.SENDING.toString());
        assertEquals("Finished", ErrorReportStatus.FINISHED.toString());
        assertEquals("Not applicable", ErrorReportStatus.NOT_APPLICABLE.toString());
    }

    @Test
    public void testTerminal() {
        assertTrue(ErrorReportStatus.WAITING.isTerminal());
        assertTrue(ErrorReportStatus.ERROR.isTerminal());
        assertFalse(ErrorReportStatus.QUEUED.isTerminal());
        assertFalse(ErrorReportStatus.SENDING.isTerminal());
        assertTrue(ErrorReportStatus.FINISHED.isTerminal());
        assertTrue(ErrorReportStatus.NOT_APPLICABLE.isTerminal());
    }

}
