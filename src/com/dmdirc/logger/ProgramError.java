/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.dmdirc.util.ClientInfo;

import com.google.common.base.MoreObjects;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Stores a program error.
 */
public final class ProgramError implements Serializable {

    /** A version number for this class. */
    private static final long serialVersionUID = 3;
    /** Semaphore used to serialise write access. */
    private static final Semaphore WRITING_SEM = new Semaphore(1);
    /** The reporter to use to send this error. */
    private final ErrorReporter reporter;
    /** Error icon. */
    private final ErrorLevel level;
    /** Error message. */
    private final String message;
    /** Underlying exception. */
    private final Throwable exception;
    /** The trace for this exception. */
    private final List<String> trace;
    /** Underlying details message. */
    private final String details;
    /** Date/time error first occurred. */
    private final Date firstDate;
    /** The error manager to register with. */
    private final ErrorManager errorManager;
    /** Date/time error last occurred. */
    private Date lastDate;
    /** Number of occurrences. */
    private final AtomicInteger count;
    /** Error report Status. */
    private ErrorReportStatus reportStatus;
    /** Has the error been output. */
    private boolean handled;
    /** Directory used to store errors. */
    private Path errorDir;

    /**
     * Creates a new instance of ProgramError.
     *
     * @param level     Error level
     * @param message   Error message
     * @param exception The exception that caused the error, if any.
     * @param trace     The textual trace for this error
     * @param details   The detailed cause of the error, if any.
     * @param date      Error time and date
     */
    public ProgramError(final ErrorLevel level, final String message,
            @Nullable final Throwable exception,
            final List<String> trace,
            @Nullable final String details,
            final Date date,
            final ClientInfo clientInfo,
            final ErrorManager errorManager) {
        checkNotNull(level);
        checkNotNull(message);
        checkNotNull(date);
        checkArgument(!message.isEmpty());

        this.level = level;
        this.message = message;
        this.exception = exception;
        this.trace = trace;
        this.details = details;
        this.firstDate = (Date) date.clone();
        this.lastDate = (Date) date.clone();
        this.count = new AtomicInteger(1);
        this.reportStatus = ErrorReportStatus.WAITING;
        this.reporter = new ErrorReporter(clientInfo);
        this.errorManager = errorManager;
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

    public String getDetails() {
        return details;
    }

    public Throwable getThrowable() {
        return exception;
    }

    /**
     * Returns this errors trace.
     *
     * @return Error trace
     */
    public List<String> getTrace() {
        return trace;
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
     * Sets the report Status of this error.
     *
     * @param newStatus new ErrorReportStatus for the error
     */
    public void setReportStatus(final ErrorReportStatus newStatus) {
        if (newStatus != null && reportStatus != newStatus) {
            reportStatus = newStatus;
            errorManager.fireErrorStatusChanged(this);

            synchronized (this) {
                notifyAll();
            }
        }
    }

    /**
     * Saves this error to disk.
     *
     * @param directory The directory to save the error in.
     */
    public void save(final Path directory) {
        try (PrintWriter out = new PrintWriter(getErrorFile(directory), true)) {
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
     * @param directory The directory to save the error in.
     *
     * @return BufferedOutputStream to write to the error file
     */
    @SuppressWarnings("PMD.SystemPrintln")
    private OutputStream getErrorFile(final Path directory) {
        WRITING_SEM.acquireUninterruptibly();

        try {
            if (errorDir == null || !Files.exists(errorDir)) {
                errorDir = directory;
                if (!Files.exists(errorDir)) {
                    Files.createDirectories(errorDir);
                }
            }

            final String logName = getDate().getTime() + "-" + getLevel();

            final Path errorFile = errorDir.resolve(logName + ".log");

            if (Files.exists(errorFile)) {
                boolean rename = false;
                for (int i = 0; !rename; i++) {
                    try {
                        Files.move(errorFile, errorDir.resolve(logName + '-' + i + ".log"));
                        rename = true;
                    } catch (IOException ex) {
                        rename = false;
                        if (i > 20) {
                            // Something's probably catestrophically wrong. Give up.
                            throw ex;
                        }
                    }
                }
            }

            Files.createFile(errorFile);
            return Files.newOutputStream(errorFile);
        } catch (IOException ex) {
            System.err.println("Error creating new file: ");
            ex.printStackTrace();
            return ByteStreams.nullOutputStream();
        } finally {
            WRITING_SEM.release();
        }
    }

    /**
     * Sends this error report to the DMDirc developers.
     */
    public void send() {
        setReportStatus(ErrorReportStatus.SENDING);
        reporter.sendException(message, level, firstDate, exception, details);
        setReportStatus(ErrorReportStatus.FINISHED);
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
        errorManager.fireErrorStatusChanged(this);

        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * Returns a human readable string describing the number of times this error occurred and when
     * these occurrences were.
     *
     * @return Occurrences description
     */
    public String occurrencesString() {
        final DateFormat format = new SimpleDateFormat("MMM dd hh:mm aa");
        if (count.get() == 1) {
            return "1 occurrence on " + format.format(getDate());
        } else {
            return count.get() + " occurrences between " + format.format(
                    getDate()) + " and " + format.format(getLastDate()) + '.';
        }
    }

    /**
     * Set this error as handled.
     */
    public void setHandled() {
        handled = true;
    }

    /**
     * Has this error been handled?
     *
     * @return Handled state
     */
    public boolean isHandled() {
        return handled;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Level", getLevel())
                .add("Status", getReportStatus())
                .add("Message", getMessage())
                .toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final ProgramError other = (ProgramError) obj;
        return Objects.equals(getLevel(), other.getLevel())
                && Objects.equals(getMessage(), other.getMessage())
                && Objects.equals(getThrowable(), other.getThrowable())
                && Objects.equals(getDetails(), other.getDetails());
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, message, exception, details);
    }

}
