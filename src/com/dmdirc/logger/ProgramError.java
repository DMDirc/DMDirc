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

import com.dmdirc.Main;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.util.Downloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Stores a program error.
 */
public final class ProgramError implements Serializable {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;

    /** Directory used to store errors. */
    private static File errorDir;

    /** Semaphore used to serialise write access. */
    private static final Semaphore writingSem = new Semaphore(1);
    
    /** Error ID. */
    private final long id;
    
    /** Error icon. */
    private final ErrorLevel level;
    
    /** Error message. */
    private final String message;
    
    /** Error trace. */
    private final String[] trace;
    
    /** Date/time error occurred. */
    private final Date date;
    
    /** Error report Status. */
    private ErrorReportStatus reportStatus;
    
    /** Error fixed Status. */
    private ErrorFixedStatus fixedStatus;
    
    /**
     * Creates a new instance of ProgramError.
     *
     * @param id error id
     * @param level Error level
     * @param message Error message
     * @param trace Error trace
     * @param date Error time and date
     */
    public ProgramError(final long id, final ErrorLevel level,
            final String message, final String[] trace, final Date date) {
        
        if (id < 0) {
            throw new IllegalArgumentException("ID must be a positive integer: " + id);
        }
        
        if (level == null) {
            throw new IllegalArgumentException("Level cannot be null");
        }
        
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or an empty string");
        }
        
        if (trace == null) {
            throw new IllegalArgumentException("Trace cannot be null");
        }
        
        if (date == null) {
            throw new IllegalArgumentException("date cannot be null");
        }
        
        this.id = id;
        this.level = level;
        this.message = message;
        this.trace = Arrays.copyOf(trace, trace.length);
        this.date = (Date) date.clone();
        this.reportStatus = ErrorReportStatus.WAITING;
        this.fixedStatus = ErrorFixedStatus.UNKNOWN;
    }
    
    /**
     * Returns this errors level.
     *
     * @return Error level
     */
    public ErrorLevel getLevel() {
        return level;
    }
    
    /**
     * Returns this errors message.
     *
     * @return Error message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Returns this errors trace.
     *
     * @return Error trace
     */
    public String[] getTrace() {
        return Arrays.copyOf(trace, trace.length);
    }
    
    /**
     * Returns this errors time.
     *
     * @return Error time
     */
    public Date getDate() {
        return (Date) date.clone();
    }
    
    /**
     * Returns the reportStatus of this error.
     * 
     * @return Error reportStatus
     */
    public ErrorReportStatus getReportStatus() {
        return reportStatus;
    }
    
    /**
     * Returns the fixed status of this error.
     * 
     * @return Error fixed status
     */
    public ErrorFixedStatus getFixedStatus() {
        return fixedStatus;
    }
    
    /**
     * Sets the report Status of this error.
     * 
     * @param newStatus new ErrorReportStatus for the error
     */
    public void setReportStatus(final ErrorReportStatus newStatus) {
        if (newStatus != null && !reportStatus.equals(newStatus)) {
            reportStatus = newStatus;
            ErrorManager.getErrorManager().fireErrorStatusChanged(this);

            synchronized (this) {
                notifyAll();
            }
        }
    }
    
    /**
     * Sets the fixed status of this error.
     * 
     * @param newStatus new ErrorFixedStatus for the error
     */
    public void setFixedStatus(final ErrorFixedStatus newStatus) {
        if (newStatus != null && !fixedStatus.equals(newStatus)) {
            fixedStatus = newStatus;
            ErrorManager.getErrorManager().fireErrorStatusChanged(this);

            synchronized (this) {
                notifyAll();
            }
        }
    }
    
    /**
     * Returns the ID of this error.
     *
     * @return Error ID
     */
    public long getID() {
        return id;
    }

    /**
     * Saves this error to disk.
     */
    public void save() {
        final PrintWriter out = new PrintWriter(getErrorFile(), true);
        out.println("Date:" + getDate());
        out.println("Level: " + getLevel());
        out.println("Description: " + getMessage());
        out.println("Details:");

        for (String traceLine : getTrace()) {
            out.println('\t' + traceLine);
        }
        
        out.close();
    }

    /**
     * Creates a new file for an error and returns the output stream.
     *
     * @return BufferedOutputStream to write to the error file
     */
    @SuppressWarnings("PMD.SystemPrintln")
    private OutputStream getErrorFile() {
        writingSem.acquireUninterruptibly();
        
        if (errorDir == null || !errorDir.exists()) {
            errorDir = new File(Main.getConfigDir() + "errors");
            if (!errorDir.exists()) {
                errorDir.mkdirs();
            }
        }

        final String logName = getDate().getTime() + "-" + getLevel();

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
            return new FileOutputStream(errorFile);
        } catch (IOException ex) {
            System.err.println("Error creating new file: ");
            ex.printStackTrace();
            return new NullOutputStream();
        } finally {
            writingSem.release();
        }
    }

    /**
     * Sends this error report to the DMDirc developers.
     */
    public void send() {
        final Map<String, String> postData = new HashMap<String, String>();
        List<String> response = new ArrayList<String>();
        int tries = 0;

        postData.put("message", getMessage());
        postData.put("trace", Arrays.toString(getTrace()));
        postData.put("version", IdentityManager.getGlobalConfig().getOption("version", "version"));

        setReportStatus(ErrorReportStatus.SENDING);

        do {
            if (tries != 0) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    //Ignore
                }
            }
            try {
                response = Downloader.getPage("http://www.dmdirc.com/error.php", postData);
            } catch (MalformedURLException ex) {
                //Ignore, wont happen
            } catch (IOException ex) {
                //Ignore being handled
            }

            tries++;
        } while ((response.isEmpty() || !response.get(response.size() - 1).
                equalsIgnoreCase("Error report submitted. Thank you."))
                && tries <= 5);

        checkResponses(response);
    }

    /**
     * Checks the responses and sets status accordingly.
     *
     * @param error Error to check response
     * @param response Response to check
     */
    private void checkResponses(final List<String> response) {
        if (!response.isEmpty() && response.get(response.size() - 1).
                equalsIgnoreCase("Error report submitted. Thank you.")) {
            setReportStatus(ErrorReportStatus.FINISHED);
        } else {
            setReportStatus(ErrorReportStatus.ERROR);
            return;
        }

        if (response.size() == 1) {
            setFixedStatus(ErrorFixedStatus.NEW);
            return;
        }

        final String responseToCheck = response.get(0);
        if (responseToCheck.matches(".*fixed.*")) {
            setFixedStatus(ErrorFixedStatus.FIXED);
        } else if (responseToCheck.matches(".*more recent version.*")) {
            setFixedStatus(ErrorFixedStatus.TOOOLD);
        } else if (responseToCheck.matches(".*invalid.*")) {
            setFixedStatus(ErrorFixedStatus.INVALID);
        } else if (responseToCheck.matches(".*previously.*")) {
            setFixedStatus(ErrorFixedStatus.KNOWN);
        } else {
            setFixedStatus(ErrorFixedStatus.NEW);
        }
    }

    /**
     * Determines whether or not the stack trace associated with this error
     * is from a valid source. A valid source is one that is within a DMDirc
     * package (com.dmdirc), and is not the DMDirc event queue.
     *
     * @return True if the source is valid, false otherwise
     */
    public boolean isValidSource() {
        final String line = getSourceLine();

        return line.startsWith("com.dmdirc")
                && !line.startsWith("com.dmdirc.addons.ui_swing.DMDircEventQueue");
    }

    /**
     * Returns the "source line" of this error, which is defined as the first
     * line starting with a DMDirc package name (com.dmdirc). If no such line
     * is found, returns the first line of the message.
     *
     * @return This error's source line
     */
    public String getSourceLine() {
        for (String line : trace) {
            if (line.startsWith("com.dmdirc")) {
                return line;
            }
        }

        return trace[0];
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ID" + id + " Level: " + getLevel() + " Status: " + getReportStatus()
        + " Message: '" + getMessage() + "'";
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        final ProgramError other = (ProgramError) obj;
        if (this.level != other.level) {
            return false;
        }
        
        if (!this.message.equals(other.message)) {
            return false;
        }
        
        if (!Arrays.equals(this.trace, other.trace)) {
            return false;
        }
        
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.level.hashCode();
        hash = 67 * hash + this.message.hashCode();
        hash = 67 * hash + Arrays.hashCode(this.trace);
        return hash;
    }
    
}
