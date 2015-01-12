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

import com.google.auto.value.AutoValue;

import java.util.Date;

/**
 * Represents an error that has occurred within the client as displayed to an end user.
 */
@AutoValue
public abstract class DisplayableError {

    DisplayableError() {}

    public abstract Date getDate();
    public abstract String getSummary();
    public abstract String getDetails();
    public abstract ErrorLevel getSeverity();
    public abstract ErrorReportStatus getReportStatus();
    public abstract ProgramError getProgramError();

    public static DisplayableError create(final Date date, final String summary,
            final String details, final ErrorLevel severity, final ErrorReportStatus status,
            final ProgramError programError) {
        return new AutoValue_DisplayableError(date, summary, details, severity, status, programError);
    }
}
