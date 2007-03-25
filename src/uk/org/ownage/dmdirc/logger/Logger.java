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

package uk.org.ownage.dmdirc.logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JDialog;

import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.ui.ErrorDialog;
import uk.org.ownage.dmdirc.ui.FatalErrorDialog;
import uk.org.ownage.dmdirc.ui.MainFrame;

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
     * dialog used to report errors to the ui.
     */
    private static JDialog dialog;
    
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
     */
    public static void error(final ErrorLevel level, final String message) {
        handleError(level, new String[]{message, });
    }
    
    /**
     * Record an error message for the application, notifying the user
     * if appropriate.
     * @param level error level.
     * @param message Error message/cause.
     */
    private static void handleError(final ErrorLevel level, final String[] message) {
        if (logWriter == null || debugWriter == null || errorWriter == null) {
            createWriters();
        }
        
        ImageIcon icon;
        
        switch (level) {
            case FATAL:
                errorWriter.println(formatter.format(new Date()) + ": ERROR: "
                        + level + " :" + message);
                dialog = new FatalErrorDialog(MainFrame.getMainFrame(), true,
                        message);
                dialog.pack();
                dialog.setLocationRelativeTo(MainFrame.getMainFrame());
                dialog.setVisible(true);
                break;
            case ERROR:
                icon = new ImageIcon(ClassLoader.getSystemClassLoader()
                .getResource("uk/org/ownage/dmdirc/res/error.png"));
                showError(level, icon, message);
                break;
            case WARNING:
                icon = new ImageIcon(ClassLoader.getSystemClassLoader()
                .getResource("uk/org/ownage/dmdirc/res/warning.png"));
                showError(level, icon, message);
                break;
            case INFO:
                icon = new ImageIcon(ClassLoader.getSystemClassLoader()
                .getResource("uk/org/ownage/dmdirc/res/info.png"));
                showError(level, icon, message);
                break;
            default:
                icon = new ImageIcon(ClassLoader.getSystemClassLoader()
                .getResource("uk/org/ownage/dmdirc/res/error.png"));
                showError(level, icon, message);
                break;
        }
    }
    
    /**
     * Shows the specified error.
     * @param level The level of the error
     * @param icon The icon to use for the status bar
     * @param message The error message itself
     */
    private static void showError(final ErrorLevel level, final ImageIcon icon, 
            final String[] message) {
        MainFrame.getMainFrame().getStatusBar().setError(icon,
                new ErrorDialog(MainFrame.getMainFrame(),
                false, message));
        errorWriter.println(formatter.format(new Date()) + ": ERROR: "
                + level + " :" + message[0]);
        if (message.length > 1) {
            for (int i = 1; i < message.length; i++) {
                errorWriter.println("\t" + message[i]);
            }
        }
        System.err.println(formatter.format(new Date()) + ": ERROR: "
                + level + " :" + message[0]);
        if (message.length > 1) {
            for (int i = 1; i < message.length; i++) {
                System.err.println("\t" + message[i]);
            }
        }
    }
    
    /**
     * Record an error message for the application at the error error level,
     * notifying the user if appropriate.
     * @param message Error message/cause.
     */
    public static void error(final String message) {
        error(ErrorLevel.ERROR, message);
    }
    
    /**
     * Record an error message for the application, notifying the user if
     * appropriate.
     * @param level error level.
     * @param exception Cause of error.
     */
    public static void error(final ErrorLevel level, final Exception exception) {
        final StackTraceElement[] stackTrace = exception.getStackTrace();
        
        String[] message;
        int i;
        
        message = new String[stackTrace.length + 1];
        message[0] = exception.toString();
        i = 1;
        for (StackTraceElement traceElement : stackTrace) {
            message[i] = "\t" + traceElement.toString();
            i++;
        }
        
        handleError(level, message);
    }
    
    /**
     * Record an error message for the application at the error error level,
     * notifying the user if appropriate.
     * @param exception Cause of error.
     */
    public static void error(final Exception exception) {
        error(ErrorLevel.ERROR, exception);
    }
    
    /**
     * Record an debug message for the application, notifying the user if
     * appropriate.
     * @param level debug level.
     * @param message Debug message.
     */
    public static void debug(final DebugLevel level, final String message) {
        if (!Config.hasOption("logging", "debugLogging")
        && !Config.getOption("logging", "debugLogging").equals("true")) {
            return;
        }
        if (logWriter == null || debugWriter == null || errorWriter == null) {
            createWriters();
        }
        
        switch(level) {
            default:
                if (Config.hasOption("logging", "debugLoggingSysOut")
                && Config.getOption("logging", "debugLoggingSysOut")
                .equals("true")) {
                    System.out.println(formatter.format(new Date())
                    + ": DEBUG: " + level + " :" + message);
                }
                
                debugWriter.println(formatter.format(new Date())
                + ": DEBUG: " + level + " :" + message);
                break;
        }
    }
    
    /**
     * Record an debug message for the application at the normal debug level,
     * notifying the user if appropriate.
     * @param message Debug message.
     */
    public static void debug(final String message) {
        debug(DebugLevel.NORMAL, message);
    }
    
    /**
     * Record a log message for the application.
     * @param level log level.
     * @param message Log message.
     */
    public static void log(final LogLevel level, final String message) {
        if (!Config.hasOption("logging", "programLogging")
        && !Config.getOption("logging", "programLogging").equals("true")) {
            return;
        }
        if (logWriter == null || debugWriter == null || errorWriter == null) {
            createWriters();
        }
        
        switch(level) {
            default:
                logWriter.println(formatter.format(new Date())
                + ": LOG: " + level + " :" + message);
                break;
        }
    }
    
    /**
     * Initialises the the loggers writers (debug, error, log) and date formatter.
     */
    private static void createWriters() {
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
            Logger.error(ErrorLevel.WARNING, ex);
        }
        if (Config.hasOption("logging", "dateFormat")) {
            formatter = new SimpleDateFormat(
                    Config.getOption("logging", "dateFormat"));
        } else {
            formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        }
    }
}
