/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

    /** The error manager to report errors to. */
    private static ErrorManager errorManager;

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
        getErrorManager().addError(level, message);
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
        getErrorManager().addError(level, message, new String[]{details}, false);
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
        getErrorManager().addError(level, message, exception, false);
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
        getErrorManager().addError(level, message, exception, true);
    }

    /**
     * Gets the manager that should be used to report errors.
     *
     * @return The error manager to be used.
     */
    private static synchronized ErrorManager getErrorManager() {
        if (errorManager == null) {
            errorManager = ErrorManager.getErrorManager();
        }

        return errorManager;
    }

    /**
     * Sets the error manager that this logger will use.
     *
     * @param errorManager The error manager to use.
     */
    public static void setErrorManager(final ErrorManager errorManager) {
        Logger.errorManager = errorManager;
    }

}
