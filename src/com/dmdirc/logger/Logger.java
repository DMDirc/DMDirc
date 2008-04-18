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

import com.dmdirc.Main;
import com.dmdirc.config.IdentityManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Logger class for the application.
 */
public final class Logger {
       
    /** Prevent instantiation of a new instance of Logger. */
    private Logger() {
        //Ignore
    }
    
    /**
     * Called when a user correctable error occurs.
     *
     * @param level Severity of the error
     * @param message Brief error description
     */
    public static void userError(final ErrorLevel level,
            final String message) {
        userError(level, message, "");
    }
    
    /**
     * Called when a user correctable error occurs.
     *
     * @param level Severity of the error
     * @param message Brief error description
     * @param details Verbose description of the error
     */
    public static void userError(final ErrorLevel level,
            final String message, final String details) {
        error(level, message, new String[]{"\n", details, }, false);
    }
    
    /**
     * Called when a user correctable error occurs.
     *
     * @param level Severity of the error
     * @param message Brief error description
     * @param exception Throwable cause for the error
     */
    public static void userError(final ErrorLevel level,
            final String message, final Throwable exception) {
        error(level, message, new String[0], false);
    }
    
    /**
     * Called when a non user correctable error occurs, the error will be
     * logged and optionally sent to the developers.
     *
     * @param level Severity of the error
     * @param message Brief error description
     * @param exception Cause of error
     */
    public static void appError(final ErrorLevel level,
            final String message, final Throwable exception) {
        error(level, message, exceptionToStringArray(exception), true);
    }
    
    /**
     * Handles an error in the program.
     *
     * @param level Severity of the error
     * @param message Brief error description
     * @param exception Cause of error
     * @param sendable Whether the error is sendable
     */
    private static void error(final ErrorLevel level,
            final String message, final String[] exception,
            final boolean sendable) {
        final ProgramError error = createError(level, message, exception);
        
        ErrorManager.getErrorManager().addError(error, sendable);
    }
    
    /**
     * Creates a new ProgramError from the supplied information, and writes
     * the error to a file.
     *
     * @param level Error level
     * @param message Error message
     * @param trace Error cause
     *
     * @return ProgramError encapsulating the supplied information
     */
    private static ProgramError createError(final ErrorLevel level,
            final String message, final String[] trace) {        
        final ProgramError error = new ProgramError(
                ErrorManager.getErrorManager().getNextErrorID(), level, message,
                trace, new Date(System.currentTimeMillis()));
                
        return error;
    }
        
    /**
     * Converts an exception into a string array.
     * 
     * @param throwable Exception to convert
     * 
     * @return Exception string array
     */
    private static String[] exceptionToStringArray(final Throwable throwable) {
        String[] trace;
        
        if (throwable == null) {
            trace = new String[0];
        } else {
            final StackTraceElement[] traceElements = throwable.getStackTrace();
            trace = new String[traceElements.length + 1];
            
            trace[0] = throwable.toString();
            
            for (int i = 0; i < traceElements.length; i++) {
                trace[i + 1] = traceElements[i].toString();
            }
            
            if (throwable.getCause() != null) {
                final String[] causeTrace = exceptionToStringArray(throwable.getCause());
                final String[] newTrace = new String[trace.length + causeTrace.length];
                trace[0] = "\nWhich caused: " + trace[0];
                
                System.arraycopy(causeTrace, 0, newTrace, 0, causeTrace.length);
                System.arraycopy(trace, 0, newTrace, causeTrace.length, trace.length);
                
                trace = newTrace;
            }
        }
        
        return trace;
    }
    
    /**
     * Asserts that the specified value is true. If not, an AssertionError
     * exception is thrown.
     * 
     * @param value The value to be tested
     */
    public static void assertTrue(final boolean value) {
        if (!value) {
            throw new AssertionError();
        }
    }
    
}
