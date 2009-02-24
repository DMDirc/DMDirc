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

/**
 * Logger class for the application.
 */
public final class Logger {

    /** The manager to use to report errors. */
    protected static final ErrorManager manager = ErrorManager.getErrorManager();
       
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
    public static void userError(final ErrorLevel level, final String message) {
        manager.addError(level, message);
    }
    
    /**
     * Called when a user correctable error occurs.
     *
     * @param level Severity of the error
     * @param message Brief error description
     * @param details Verbose description of the error
     */
    public static void userError(final ErrorLevel level, final String message,
            final String details) {
        manager.addError(level, message, new String[]{details}, false);
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
        manager.addError(level, message, exception, false);
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
        manager.addError(level, message, exception, true);
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
