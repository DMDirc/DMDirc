/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.core.util.Info;
import com.dmdirc.util.io.Downloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import net.kencochrane.raven.Raven;
import net.kencochrane.raven.RavenFactory;
import net.kencochrane.raven.dsn.Dsn;
import net.kencochrane.raven.event.Event;
import net.kencochrane.raven.event.EventBuilder;
import net.kencochrane.raven.event.interfaces.ExceptionInterface;

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
    private static final Semaphore WRITING_SEM = new Semaphore(1);

    public static final String SENTRY_DSN = "http://d53a31a3c53c4a4f91c5ff503e612677:e0a8aa1ecca14568a9f52d052ecf6a30@sentry.dmdirc.com/2";

    /** Error ID. */
    private final long id;

    /** Error icon. */
    private final ErrorLevel level;

    /** Error message. */
    private final String message;

    /** Underlying exception. */
    private final Throwable exception;

    /** Underlying details message. */
    private final String details;

    /** Date/time error first occurred. */
    private final Date firstDate;

    /** Date/time error last occurred. */
    private Date lastDate;

    /** Number of occurrences. */
    private AtomicInteger count;

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
     * @param exception The exception that caused the error, if any.
     * @param details The detailed cause of the error, if any.
     * @param date Error time and date
     */
    public ProgramError(final long id, final ErrorLevel level, final String message,
            @Nullable final Throwable exception,
            @Nullable final String details,
            final Date date) {

        if (id < 0) {
            throw new IllegalArgumentException("ID must be a positive integer: " + id);
        }

        if (level == null) {
            throw new IllegalArgumentException("Level cannot be null");
        }

        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or an empty string");
        }

        if (date == null) {
            throw new IllegalArgumentException("date cannot be null");
        }

        this.id = id;
        this.level = level;
        this.message = message;
        this.exception = exception;
        this.details = details;
        this.firstDate = (Date) date.clone();
        this.lastDate = (Date) date.clone();
        this.count = new AtomicInteger(1);
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
        return exception != null ? getTrace(exception) :
                message != null ? new String[] { message } :
                new String[0];
    }

    /**
     * Returns this errors time.
     *
     * @return Error time
     */
    public Date getDate() {
        return (Date) firstDate.clone();
    }

    /**
     * Returns the number of times this error has occurred.
     *
     * @return Error count
     */
    public int getCount() {
        return count.get();
    }

    /**
     * Returns the last time this error occurred.
     *
     * @return Last occurrence
     */
    public Date getLastDate() {
        return (Date) lastDate.clone();
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
        try (PrintWriter out = new PrintWriter(getErrorFile(), true)) {
            out.println("Date:" + getDate());
            out.println("Level: " + getLevel());
            out.println("Description: " + getMessage());
            out.println("Details:");

            for (String traceLine : getTrace()) {
                out.println('\t' + traceLine);
            }
        }
    }

    /**
     * Creates a new file for an error and returns the output stream.
     *
     * @return BufferedOutputStream to write to the error file
     */
    @SuppressWarnings("PMD.SystemPrintln")
    private OutputStream getErrorFile() {
        WRITING_SEM.acquireUninterruptibly();

        if (errorDir == null || !errorDir.exists()) {
            errorDir = new File(IdentityManager.getIdentityManager().getConfigurationDirectory() + "errors");
            if (!errorDir.exists()) {
                errorDir.mkdirs();
            }
        }

        final String logName = getDate().getTime() + "-" + getLevel();

        final File errorFile = new File(errorDir, logName + ".log");

        if (errorFile.exists()) {
            boolean rename = false;
            for (int i = 0; !rename; i++) {
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
            WRITING_SEM.release();
        }
    }

    /**
     * Sends this error report to the DMDirc developers.
     */
    public void send() {
        sendToSentry();
        sendToLegacy();
    }

    private void sendToSentry() {
        final Raven raven = RavenFactory.ravenInstance(new Dsn(SENTRY_DSN));

        // record a simple message
        final EventBuilder eventBuilder = new EventBuilder()
                .setMessage(message)
                .setLevel(getSentryLevel())
                .setServerName("")
                .setTimestamp(firstDate)
                .addTag("version", getVersion())
                .addTag("version.major", getVersion().replaceAll("-.*", ""))
                .addTag("os.name", System.getProperty("os.name", "unknown"))
                .addTag("os.version", System.getProperty("os.version", "unknown"))
                .addTag("os.arch", System.getProperty("os.arch", "unknown"))
                .addTag("encoding", System.getProperty("file.encoding", "unknown"))
                .addTag("locale", Locale.getDefault().toString())
                .addTag("jvm.name", System.getProperty("java.vm.name", "unknown"))
                .addTag("jvm.vendor", System.getProperty("java.vm.vendor", "unknown"))
                .addTag("jvm.version", System.getProperty("java.version", "unknown"))
                .addTag("jvm.version.major", System.getProperty("java.version", "unknown").replaceAll("_.*", ""));

        if (exception != null) {
            eventBuilder.addSentryInterface(new ExceptionInterface(exception));
        }

        if (details != null) {
            eventBuilder.addExtra("details", details);
        }

        raven.sendEvent(eventBuilder.build());
    }

    private void sendToLegacy() {
        final Map<String, String> postData = new HashMap<>();
        List<String> response = new ArrayList<>();
        int tries = 0;

        String traceString = Arrays.toString(getTrace());
        if (traceString.isEmpty() || traceString.equals("[]")) {
            traceString = "[No Trace]";
        }
        postData.put("message", getMessage());
        postData.put("trace", traceString);
        postData.put("version", IdentityManager.getIdentityManager()
                .getGlobalConfiguration().getOption("version", "version"));

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
        final String[] trace = getTrace();

        for (String line : trace) {
            if (line.startsWith("com.dmdirc")) {
                return line;
            }
        }

        return trace[0];
    }

    /**
     * Updates the last date this error occurred.
     */
    public void updateLastDate() {
        updateLastDate(new Date());
    }

    /**
     * Updates the last date this error occurred.
     *
     * @param date Date error occurred
     */
    public void updateLastDate(final Date date) {
        lastDate = date;
        count.getAndIncrement();
        ErrorManager.getErrorManager().fireErrorStatusChanged(this);

        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * Retruns a human readable string describing the number of times
     * this error occurred and when these occurrences were.
     *
     * @return Occurrences description
     */
    public String occurrencesString() {
        final DateFormat format = new SimpleDateFormat("MMM dd hh:mm aa");
        if (count.get() == 1) {
            return "1 occurrence on " + format.format(getDate());
        } else {
            return count.get() + " occurrences between " + format.format(
                    getDate()) + " and " + format.format(getLastDate()) + ".";
        }
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

        if (!Objects.equals(this.exception, other.exception)) {
            return false;
        }

        if (!Objects.equals(this.details, other.details)) {
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
        hash = 67 * hash + (this.exception == null ? 1 : this.exception.hashCode());
        hash = 67 * hash + (this.details == null ? 1 : this.details.hashCode());
        return hash;
    }


    /**
     * Converts an exception into a string array.
     *
     * @param throwable Exception to convert
     * @since 0.6.3m1
     * @return Exception string array
     */
    private static String[] getTrace(final Throwable throwable) {
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
                final String[] causeTrace = getTrace(throwable.getCause());
                final String[] newTrace = new String[trace.length + causeTrace.length];
                trace[0] = "\nWhich caused: " + trace[0];

                System.arraycopy(causeTrace, 0, newTrace, 0, causeTrace.length);
                System.arraycopy(trace, 0, newTrace, causeTrace.length, trace.length);

                trace = newTrace;
            }
        }

        return trace;
    }

    private Event.Level getSentryLevel() {
        switch (level) {
            case FATAL:
                return Event.Level.FATAL;
            case HIGH:
                return Event.Level.ERROR;
            case MEDIUM:
                return Event.Level.WARNING;
            case LOW:
                return Event.Level.INFO;
            default:
                return Event.Level.INFO;
        }
    }

    private String getVersion() {
        return IdentityManager.getIdentityManager().getGlobalConfiguration()
                .getOption("version", "version");
    }

}
