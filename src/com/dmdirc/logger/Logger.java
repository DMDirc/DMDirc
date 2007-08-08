/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.Config;
import com.dmdirc.Main;

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
    
    /** ProgramError folder. */
    private static File errorDir;
    
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
        error(level, message, null, false);
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
        error(level, message, exception, true);
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
            final String message, final Throwable exception,
            final boolean sendable) {
        final ProgramError error = createError(level, message, exception);
        
        if (!sendable) {
            error.setStatus(ErrorStatus.NOT_APPLICABLE);
        }
        
        if (sendable && Config.getOptionBool("general", "submitErrors")) {
            ErrorManager.getErrorManager().sendError(error);
        }
        
        if (level == ErrorLevel.FATAL && !Config.getOptionBool("general", "submitErrors")) {
            error.setStatus(ErrorStatus.FINISHED);
        }
        
        ErrorManager.getErrorManager().addError(error);
    }
    
    /**
     * Creates a new ProgramError from the supplied information, and writes
     * the error to a file.
     *
     * @param level Error level
     * @param message Error message
     * @param exception Error cause
     *
     * @return ProgramError encapsulating the supplied information
     */
    private static ProgramError createError(final ErrorLevel level,
            final String message, final Throwable exception) {
        final String[] trace;
        final StackTraceElement[] traceElements;
        
        if (exception == null) {
            trace = new String[0];
        } else {
            traceElements = exception.getStackTrace();
            trace = new String[traceElements.length + 1];
            
            trace[0] = exception.toString();
            
            for (int i = 0; i < traceElements.length; i++) {
                trace[i + 1] = traceElements[i].toString();
            }
        }
        
        final ProgramError error = new ProgramError(
                ErrorManager.getErrorManager().getErrorCount(), level, message, 
                trace, new Date(System.currentTimeMillis()));
        
        writeError(error);
        
        return error;
    }
    
    /**
     * Writes the specified error to a file.
     *
     * @param error ProgramError to write to a file.
     */
    private static void writeError(final ProgramError error) {
        final PrintWriter out = new PrintWriter(createNewErrorFile(error), true);
        out.println("Date:" + error.getDate());
        out.println("Level: " + error.getLevel());
        out.println("Description: " + error.getMessage());
        out.println("Details:");
        final String[] trace = error.getTrace();
        for (String traceLine : trace) {
            out.println('\t' + traceLine);
        }
        out.close();
    }
    
    /**
     * Creates a new file for an error and returns the output stream.
     *
     * @param error Error to create file for
     *
     * @return BufferedOutputStream to write to the error file
     */
    @SuppressWarnings("PMD.SystemPrintln")
    private static synchronized OutputStream createNewErrorFile(final ProgramError error) {
        if (errorDir == null) {
            errorDir = new File(Main.getConfigDir() + "errors");
            if (!errorDir.exists()) {
                errorDir.mkdirs();
            }
        }
        final String logName = error.getDate().getTime() + "-" + error.getLevel();
        
        
        final File errorFile = new File(errorDir, logName + ".log");
        
        if (errorFile.exists()) {
            boolean rename = false;
            int i = 0;
            while (!rename) {
                i++;
                rename = errorFile.renameTo(new File(errorDir, logName + "-" + i + ".log"));
            }
        }
        try {
            errorFile.createNewFile();
        } catch (IOException ex) {
            System.err.println("Error creating new file: ");
            ex.printStackTrace(System.err);
            return new NullOutputStream();
        }
        
        try {
            return new FileOutputStream(errorFile);
        } catch (FileNotFoundException ex) {
            System.err.println("Error creating new stream: ");
            ex.printStackTrace(System.err);
            return new NullOutputStream();
        }
    }
    
}
