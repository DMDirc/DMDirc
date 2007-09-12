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
    private final int id;
    
    /** Error icon. */
    private final ErrorLevel level;
    
    /** Error message. */
    private final String message;
    
    /** Error trace. */
    private final String[] trace;
    
    /** Date/time error occurred. */
    private final Date date;
    
    /** Error status. */
    private ErrorStatus status;
    
    /**
     * Creates a new instance of ProgramError.
     *
     * @param id error id
     * @param level Error level
     * @param message Error message
     * @param trace Error trace
     * @param date Error time and date
     */
    public ProgramError(final int id, final ErrorLevel level,
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
        if (trace == null || trace.length == 0) {
            throw new IllegalArgumentException("Trace cannot be null or an empty array");
        }
        if (date == null) {
            throw new IllegalArgumentException("date cannot be null");
        }
        this.id = id;
        this.level = level;
        this.message = message;
        this.trace = Arrays.copyOf(trace, trace.length);
        this.date = (Date) date.clone();
        this.status = ErrorStatus.WAITING;
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
     * Returns the status of this error.
     *
     * @return Error status
     */
    public ErrorStatus getStatus() {
        return status;
    }
    
    /**
     * Sets the status of this error.
     *
     * @param newStatus new ErrorStatus for the error
     */
    public void setStatus(final ErrorStatus newStatus) {
        if (newStatus != null && !status.equals(newStatus)) {
            status = newStatus;
            ErrorManager.getErrorManager().fireErrorStatusChanged(this);
        }
    }
    
    /**
     * Returns the ID of this error.
     *
     * @return Error ID
     */
    public int getID() {
        return id;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ID" + id + " Level: " + getLevel() + " Status: " + getStatus()
        + " Message: '" + getMessage() + "'";
    }
    
}
