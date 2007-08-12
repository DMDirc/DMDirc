/*
 * ProgramErrorTest.java
 * JUnit based test
 *
 * Created on 08 April 2007, 13:20
 */

package com.dmdirc;

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.ErrorStatus;
import com.dmdirc.logger.ProgramError;

import java.util.Arrays;
import java.util.Date;

import junit.framework.*;

/**
 * Tests the ProgramError class
 */
public class ProgramErrorTest extends TestCase {
    
    private ErrorLevel level;
    private String message;
    private String[] trace;
    private Date date;
    
    public ProgramErrorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        level = ErrorLevel.HIGH;
        message = "Test error";
        trace = new String[]{"line 1", "line 2", };
        date = new Date(System.currentTimeMillis());
    }
    
    public void testGetLevel() {
        final ProgramError inst = new ProgramError(0, level, message, trace, date);
        assertEquals("Level check failed.", level, inst.getLevel());
    }
    
    public void testGetMessage() {
        final ProgramError inst = new ProgramError(0, level, message, trace, date);
        assertEquals("Message check failed.", message, inst.getMessage());
    }
    
    public void testGetTrace() {
        final ProgramError inst = new ProgramError(0, level, message, trace, date);
        assertTrue("Trace check failed", Arrays.equals(trace, inst.getTrace())); //NOPMD
    }
    
    public void testGetDate() {
        final ProgramError inst = new ProgramError(0, level, message, trace, date);
        assertTrue("Date check after failed.", inst.getDate().after(new Date(date.getTime() - 1)));
        assertTrue("Date check before failed.", inst.getDate().before(new Date(date.getTime() + 1)));
    }
    
    public void testGetStatus() {
        final ProgramError inst = new ProgramError(0, level, message, trace, date);
        assertEquals("Get status check failed.", ErrorStatus.WAITING, inst.getStatus());
    }
    
    public void testSetStatus() {
        final ProgramError inst = new ProgramError(0, level, message, trace, date);
        assertEquals("Get status check failed.", ErrorStatus.WAITING, inst.getStatus());
        inst.setStatus(ErrorStatus.FINISHED);
        assertEquals("Set status check failed.", ErrorStatus.FINISHED, inst.getStatus());
    }
    
}
