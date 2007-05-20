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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.ImageIcon;

import com.dmdirc.Config;
import com.dmdirc.ui.MainFrame;
import com.dmdirc.ui.dialogs.ErrorDialog;

/**
 * Logger class for an applications, provides logging, error logging and debug
 * logging.
 */
public final class Logger {
    
    /**
     * Logging Printwriter.
     */
    private static PrintWriter logWriter;
    
    /**
     * Debug Printwriter.
     */
    private static PrintWriter debugWriter;
    
    /**
     * Error Printwriter.
     */
    private static PrintWriter errorWriter;
    
    /**
     * Date formatter, used for logging and displaying messages.
     */
    private static SimpleDateFormat formatter;
    
    /**
     * Prevents creation a new instance of Logger.
     */
    private Logger() {
    }
    
    /**
     * Record an error message for the application, notifying the user
     * if appropriate.
     * @param level error level.
     * @param message Error message/cause.
     * @param trace error trace.
     */
    private static void handleError(final ErrorLevel level, final String message, final String[] trace) {
        if (logWriter == null || debugWriter == null || errorWriter == null) {
            createWriters();
        }
        
        ImageIcon icon;
        
        switch (level) {
            case FATAL:
                icon = new ImageIcon(ClassLoader.getSystemClassLoader()
                .getResource("uk/org/ownage/dmdirc/res/error.png"));
                break;
            case ERROR:
                icon = new ImageIcon(ClassLoader.getSystemClassLoader()
                .getResource("uk/org/ownage/dmdirc/res/error.png"));
                break;
            case WARNING:
                icon = new ImageIcon(ClassLoader.getSystemClassLoader()
                .getResource("uk/org/ownage/dmdirc/res/warning.png"));
                break;
            case TRIVIAL:
                icon = new ImageIcon(ClassLoader.getSystemClassLoader()
                .getResource("uk/org/ownage/dmdirc/res/info.png"));
                break;
            default:
                icon = new ImageIcon(ClassLoader.getSystemClassLoader()
                .getResource("uk/org/ownage/dmdirc/res/error.png"));
                break;
        }
        showError(level, icon, message, trace);
    }
    
    /**
     * Shows the specified error.
     * @param level The level of the error
     * @param icon The icon to use for the status bar
     * @param message The error message itself
     * @param trace error trace.
     */
    private static void showError(final ErrorLevel level, final ImageIcon icon,
            final String message, final String[] trace) {
        boolean autoSubmit = false;
        
        if (Config.getOptionBool("general", "autoSubmitErrors")) {
            autoSubmit = true;
        }
        
        final ErrorDialog errorDialog =
                new ErrorDialog(level, icon, message, trace, autoSubmit);
        MainFrame.getMainFrame().getStatusBar().setError(icon, errorDialog);
        if (level == ErrorLevel.FATAL) {
            errorDialog.clickReceived();
        }
        if (trace.length > 0) {
            synchronized (formatter) {
                errorWriter.println(formatter.format(new Date()) + ": ERROR: "
                        + level + " :" + trace[0]);
                if (trace.length > 1) {
                    for (int i = 1; i < trace.length; i++) {
                        errorWriter.println(trace[i]);
                    }
                }
            }
        }
    }
    
    /**
     * Record an error message for the application, notifying the user
     * if appropriate.
     * @param level error level.
     * @param message Error message/cause.
     */
    public static void error(final ErrorLevel level, final String message) {
        handleError(level, message, new String[0]);
    }
       
    /**
     * Record an error message for the application, notifying the user if
     * appropriate.
     * @param level error level.
     * @param exception Cause of error.
     * @param message Error message.
     */
    public static void error(final ErrorLevel level, final String message,
            final Throwable exception) {
        final StackTraceElement[] stackTrace = exception.getStackTrace();
        
        String[] stackTraceMessage;
        int i;
        
        stackTraceMessage = new String[stackTrace.length + 1];
        stackTraceMessage[0] = exception.toString();
        i = 1;
        for (StackTraceElement traceElement : stackTrace) {
            stackTraceMessage[i] = traceElement.toString();
            i++;
        }
        handleError(level, message, stackTraceMessage);
    }    
    
    /**
     * Initialises the the loggers writers (debug, error, log) and date formatter.
     */
    private static synchronized void createWriters() {
        try {
            if (logWriter == null) {
                logWriter = new PrintWriter(
                        new FileWriter(Config.getConfigDir()
                        + "log.log", true), true);
            }
            if (debugWriter == null) {
                debugWriter = new PrintWriter(
                        new FileWriter(Config.getConfigDir()
                        + "debug.log", true), true);
            }
            if (errorWriter == null) {
                errorWriter = new PrintWriter(
                        new FileWriter(Config.getConfigDir()
                        + "error.log", true), true);
            }
        } catch (IOException ex) {
            Logger.error(ErrorLevel.WARNING, "Error creating logfiles", ex);
        }
        if (Config.hasOption("logging", "dateFormat")) {
            formatter = new SimpleDateFormat(
                    Config.getOption("logging", "dateFormat"), Locale.getDefault());
        } else {
            formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.getDefault());
        }
    }
}
