/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.logger;

import com.dmdirc.config.provider.AggregateConfigProvider;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Error manager.
 */
@Singleton
public class ErrorManager {

    private final SentryLoggingErrorManager sentryLoggingErrorManager;
    private final ProgramErrorManager programErrorManager;
    private final DiskLoggingErrorManager diskLoggingErrorManager;

    @Inject
    public ErrorManager(
            final SentryLoggingErrorManager sentryLoggingErrorManager,
            final ProgramErrorManager programErrorManager,
            final DiskLoggingErrorManager diskLoggingErrorManager) {
        this.sentryLoggingErrorManager = sentryLoggingErrorManager;
        this.programErrorManager = programErrorManager;
        this.diskLoggingErrorManager = diskLoggingErrorManager;
    }

    public void initialise(final AggregateConfigProvider config) {
        sentryLoggingErrorManager.initialise(config);
        diskLoggingErrorManager.initialise(config);
        programErrorManager.initialise();
    }

    public Set<ProgramError> getErrors() {
        return programErrorManager.getErrors();
    }

    public void deleteError(final ProgramError error) {
        programErrorManager.deleteError(error);
    }

    public void deleteAll() {
        programErrorManager.deleteAll();
    }

    public void sendError(final ProgramError error) {
        sentryLoggingErrorManager.sendError(error);
    }
}
