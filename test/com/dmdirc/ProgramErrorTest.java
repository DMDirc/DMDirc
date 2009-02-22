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

package com.dmdirc;

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.ErrorReportStatus;
import com.dmdirc.logger.ProgramError;

import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProgramErrorTest {
    
    private ErrorLevel level;
    private String message;
    private String[] trace;
    private Date date;
    

    @Before
    public void setUp() throws Exception {
        level = ErrorLevel.HIGH;
        message = "Test error";
        trace = new String[]{"line 1", "line 2", };
        date = new Date(System.currentTimeMillis());
    }
    
    @Test
    public void testGetLevel() {
        final ProgramError inst = new ProgramError(0, level, message, trace, date);
        assertEquals("Level check failed.", level, inst.getLevel());
    }
    
    @Test
    public void testGetMessage() {
        final ProgramError inst = new ProgramError(0, level, message, trace, date);
        assertEquals("Message check failed.", message, inst.getMessage());
    }
    
    @Test
    public void testGetTrace() {
        final ProgramError inst = new ProgramError(0, level, message, trace, date);
        assertTrue("Trace check failed", Arrays.equals(trace, inst.getTrace())); //NOPMD
    }
    
    @Test
    public void testGetDate() {
        final ProgramError inst = new ProgramError(0, level, message, trace, date);
        assertTrue("Date check after failed.", inst.getDate().after(new Date(date.getTime() - 1)));
        assertTrue("Date check before failed.", inst.getDate().before(new Date(date.getTime() + 1)));
    }
    
    @Test
    public void testGetStatus() {
        final ProgramError inst = new ProgramError(0, level, message, trace, date);
        assertEquals("Get status check failed.", ErrorReportStatus.WAITING, inst.getReportStatus());
    }
    
    @Test
    public void testSetStatus() {
        final ProgramError inst = new ProgramError(0, level, message, trace, date);
        assertEquals("Get status check failed.", ErrorReportStatus.WAITING, inst.getReportStatus());
        inst.setReportStatus(ErrorReportStatus.FINISHED);
        assertEquals("Set status check failed.", ErrorReportStatus.FINISHED, inst.getReportStatus());
    }
    
}
