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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.ProgramErrorStatusEvent;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Stores a program error.
 */
public class ProgramError implements Serializable {

    /** A version number for this class. */
    private static final long serialVersionUID = 4;
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
    /** The eventbus to post status changes to. */
    private final DMDircMBassador eventBus;
    /** Is this an application error? */
    private final boolean appError;
    /** Error report Status. */
    private ErrorReportStatus reportStatus;
    /** Has the error been output. */
    private boolean handled;

    /**
     * Creates a new instance of ProgramError.
     *
     * @param level     Error level
     * @param message   Error message
     * @param exception The exception that caused the error, if any.
     * @param trace     The textual trace for this error
     * @param details   The detailed cause of the error, if any.
     * @param date      Error time and date
     * @param eventBus  The event bus to post status changes to
     */
    public ProgramError(final ErrorLevel level, final String message,
            @Nullable final Throwable exception,
            final Iterable<String> trace,
            @Nullable final String details,
            final Date date,
            final DMDircMBassador eventBus,
            final boolean appError) {
        checkNotNull(level);
        checkNotNull(date);

        this.level = level;
        this.message = message;
        this.exception = exception;
        this.trace = Lists.newArrayList(trace);
        this.details = details;
        this.firstDate = (Date) date.clone();
        this.reportStatus = ErrorReportStatus.WAITING;
        this.eventBus = eventBus;
        this.appError = appError;
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
        return Collections.unmodifiableList(trace);
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
            eventBus.publishAsync(new ProgramErrorStatusEvent(this));
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

    /**
     * Is this an application error?
     *
     * @return true iif this is an application error
     */
    public boolean isAppError() {
        return appError;
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
