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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.ui.MainFrame;

/**
 *
 * @author greboid
 */
public class Logger {
    
    private static PrintWriter logWriter = null;
    
    private static JDialog dialog;
    
    private static JOptionPane optionPane;
    
    private static SimpleDateFormat format;
    
    /** Creates a new instance of Logger */
    private Logger() {
    }
    
    public static void error(ErrorLevel level, String message) {
        if (logWriter == null ) createWriter();
        switch (level) {
            case FATAL:
                logWriter.println(format.format(new Date())+": ERROR: "+": "+level+" :"+message);
                optionPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE,
                        JOptionPane.DEFAULT_OPTION);
                dialog = new JDialog(MainFrame.getMainFrame(), "Fatal Error",
                        true);
                dialog.setContentPane(optionPane);
                dialog.setDefaultCloseOperation(
                        JDialog.DO_NOTHING_ON_CLOSE);
                dialog.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent we) {
                        System.exit(-1);
                    }
                });
                optionPane.addPropertyChangeListener(
                        new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent e) {
                        if (dialog.isVisible() && e.getSource() == optionPane) {
                            System.exit(-1);
                        }
                    }
                });
                dialog.pack();
                dialog.setLocationRelativeTo(MainFrame.getMainFrame());
                dialog.setVisible(true);
                break;
            default:
                logWriter.println(format.format(new Date())+": ERROR: "+": "+level+" :"+message);
                break;
        }
    }
    
    public static void error(ErrorLevel level, Exception exception) {
        if (logWriter == null ) createWriter();
        StackTraceElement[] stacktrace = exception.getStackTrace();
        switch (level) {
            case FATAL:
                logWriter.println(format.format(new Date())+": ERROR: "+": "+level+" :"+exception.getMessage());
                for (StackTraceElement traceElement: stacktrace) {
                    logWriter.println("\t\t\t\t"+traceElement);
                }
                optionPane = new JOptionPane(exception.getMessage(), JOptionPane.ERROR_MESSAGE,
                        JOptionPane.DEFAULT_OPTION);
                dialog = new JDialog(MainFrame.getMainFrame(), "Fatal Error",
                        true);
                dialog.setContentPane(optionPane);
                dialog.setDefaultCloseOperation(
                        JDialog.DO_NOTHING_ON_CLOSE);
                dialog.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent we) {
                        System.exit(-1);
                    }
                });
                optionPane.addPropertyChangeListener(
                        new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent e) {
                        if (dialog.isVisible() && e.getSource() == optionPane) {
                            System.exit(-1);
                        }
                    }
                });
                dialog.pack();
                dialog.setLocationRelativeTo(MainFrame.getMainFrame());
                dialog.setVisible(true);
                break;
            default:
                logWriter.println(format.format(new Date())+": ERROR: "+": "+level+" :"+exception.getMessage());
                for (StackTraceElement traceElement: stacktrace) {
                    logWriter.println("\t\t\t\t"+traceElement);
                }
                break;
        }
    }
    
    public static void debug(DebugLevel level, String message) {
        if (logWriter == null ) createWriter();
        switch(level) {
            default:
                System.out.println(format.format(new Date())+": DEBUG: "+": "+level+" :"+message);
                logWriter.println(format.format(new Date())+": DEBUG: "+": "+level+" :"+message);
                break;
        }
    }
    
    public static void log(LogLevel level, String message) {
        if (logWriter == null ) createWriter();
        switch(level) {
            default:
                logWriter.println(format.format(new Date())+": LOG: "+": "+level+" :"+message);
                break;
        }
    }
    
    private static void createWriter() {
        try {
            logWriter = new PrintWriter(new BufferedWriter(new FileWriter(Config.getConfigDir()+"errors.log")));
        } catch (IOException ex) {
            Logger.error(ErrorLevel.FATAL, "Oh god, i cant open the error log.");
        }
        if (Config.hasOption("logging", "dateFormat")) {
            format = new SimpleDateFormat(Config.getOption("logging", "dateFormat"));
        } else {
            format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        }
    }
}
