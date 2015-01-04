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
import com.dmdirc.events.ErrorEvent;
import com.dmdirc.events.ProgramErrorDeletedEvent;
import com.dmdirc.events.ProgramErrorEvent;

import com.google.common.base.Throwables;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.engio.mbassy.listener.Handler;

/**
 * Listens for {@link ErrorEvent}s, creates {@link ProgramError}s and raises {@link
 * ProgramErrorEvent}s.
 */
public class ProgramErrorManager {

    /** The event bus to listen for errors on. */
    private final DMDircMBassador eventBus;
    /** The current list of errors. */
    private final List<ProgramError> errors;
    /** Factory to create {@link ProgramError}s. */
    private final ProgramErrorFactory programErrorFactory;

    /**
     * Creates a new instance of this error manager.
     *
     * @param eventBus        The event bus to listen to errors on
     */
    public ProgramErrorManager(final DMDircMBassador eventBus,
            final ProgramErrorFactory programErrorFactory) {
        this.eventBus = eventBus;
        this.programErrorFactory = programErrorFactory;
        errors = new CopyOnWriteArrayList<>();
    }

    /**
     * Initialises the error manager.  Must be called before logging will start.
     */
    public void initialise() {
        eventBus.subscribe(this);
    }

    @Handler
    void handleErrorEvent(final ErrorEvent event) {
        addError(event.getLevel(), event.getMessage(), event.getThrowable(), event.getDetails(),
                false);
    }

    /**
     * Adds a new error to the manager with the specified details.
     *
     * @param level     The severity of the error
     * @param message   The error message
     * @param throwable The exception that caused the error, if any.
     * @param details   The details of the exception, if any.
     * @param appError  Whether or not this is an application error
     *
     * @since 0.6.3m1
     */
    private ProgramError addError(final ErrorLevel level, final String message,
            final Throwable throwable, final String details, final boolean appError) {

        final ProgramError error = programErrorFactory.create(level, message, throwable,
                getTrace(message, throwable), details, new Date(), null, appError);
        errors.add(error);
        return error;
    }

    /**
     * Returns this errors trace.
     *
     * @return Error trace
     */
    private Iterable<String> getTrace(final String message, final Throwable throwable) {
        return Arrays.asList(throwable == null ? message == null ? new String[0]
                : new String[]{message} : Throwables.getStackTraceAsString(throwable).split("\n"));
    }

    /**
     * Called when an error needs to be deleted from the list.
     *
     * @param error ProgramError that changed
     */
    public void deleteError(final ProgramError error) {
        errors.remove(error);
        eventBus.publishAsync(new ProgramErrorDeletedEvent(error));
    }

    /**
     * Deletes all errors from the manager.
     *
     * @since 0.6.3m1
     */
    public void deleteAll() {
        errors.forEach(e -> eventBus.publishAsync(new ProgramErrorDeletedEvent(e)));
        errors.clear();
    }

    /**
     * Returns the list of program errors.
     *
     * @return Program error list
     */
    public List<ProgramError> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}
