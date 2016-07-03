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

package com.dmdirc.ui.core.errors;

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.ErrorReportStatus;
import com.dmdirc.logger.ProgramError;

import com.google.common.base.MoreObjects;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an error that has occurred within the client as displayed to an end user.
 */
public class DisplayableError {

    private final LocalDateTime date;
    private final String summary;
    private final String details;
    private final ErrorLevel severity;
    private final ProgramError programError;
    private ErrorReportStatus reportStatus;

    public DisplayableError(final LocalDateTime date, final String summary, final String details,
            final ErrorLevel severity, final ErrorReportStatus reportStatus,
            final ProgramError programError) {
        this.date = date;
        this.summary = summary;
        this.details = details;
        this.severity = severity;
        this.programError = programError;
        this.reportStatus = reportStatus;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getSummary() {
        return summary;
    }

    public String getDetails() {
        return details;
    }

    public ErrorLevel getSeverity() {
        return severity;
    }

    public ProgramError getProgramError() {
        return programError;
    }

    public ErrorReportStatus getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(final ErrorReportStatus reportStatus) {
        this.reportStatus = reportStatus;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Severity", getSeverity())
                .add("Summary", getSummary())
                .toString();
    }

    @Override
    public boolean equals(final Object object) {
        if (object instanceof DisplayableError) {
            final DisplayableError e = (DisplayableError) object;
            return Objects.equals(getDate(), e.getDate())
                    && Objects.equals(getSummary(), e.getSummary())
                    && Objects.equals(getDetails(), e.getDetails())
                    && Objects.equals(getSeverity(), e.getSeverity());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDate(), getSummary(), getDetails(), getSeverity());
    }
}
