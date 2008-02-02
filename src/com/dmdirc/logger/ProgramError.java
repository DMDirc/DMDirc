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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

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
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ID" + id + " Level: " + getLevel() + " Status: " + getReportStatus()
        + " Message: '" + getMessage() + "'";
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
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
        if (this.message == null || !this.message.equals(other.message)) {
            return false;
        }
        if (this.trace == null || !Arrays.equals(this.trace, other.trace)) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.level != null ? this.level.hashCode() : 0);
        hash = 67 * hash + (this.message != null ? this.message.hashCode() : 0);
        hash = 67 * hash + (this.trace != null ? Arrays.hashCode(this.trace) : 0);
        return hash;
    }
    
    
    
}
