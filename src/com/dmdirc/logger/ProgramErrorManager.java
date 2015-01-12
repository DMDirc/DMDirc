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
import com.dmdirc.events.FatalProgramErrorEvent;
import com.dmdirc.events.NonFatalProgramErrorEvent;
import com.dmdirc.events.ProgramErrorDeletedEvent;
import com.dmdirc.events.ProgramErrorEvent;
import com.dmdirc.util.EventUtils;

import com.google.common.base.Throwables;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.engio.mbassy.listener.Handler;

/**
 * Listens for {@link ErrorEvent}s, creates {@link ProgramError}s and raises {@link
 * ProgramErrorEvent}s.
 */
@Singleton
public class ProgramErrorManager {

    /** A list of exceptions which we don't consider bugs and thus don't report. */
    private static final Class<?>[] BANNED_EXCEPTIONS = new Class<?>[]{
            NoSuchMethodError.class, NoClassDefFoundError.class,
            UnsatisfiedLinkError.class, AbstractMethodError.class,
            IllegalAccessError.class, OutOfMemoryError.class,
            NoSuchFieldError.class,};
    /** The event bus to listen for errors on. */
    private final DMDircMBassador eventBus;
    /** The current list of errors. */
    private final Set<ProgramError> errors;
    /** Factory to create {@link ProgramError}s. */
    private final ProgramErrorFactory programErrorFactory;

    @Inject
    public ProgramErrorManager(final DMDircMBassador eventBus,
            final ProgramErrorFactory programErrorFactory) {
        this.eventBus = eventBus;
        this.programErrorFactory = programErrorFactory;
        errors = new CopyOnWriteArraySet<>();
    }

    /**
     * Initialises the error manager.  Must be called before logging will start.
     */
    public void initialise() {
        eventBus.subscribe(this);
    }

    @Handler
    void handleErrorEvent(final ErrorEvent event) {
        final ProgramError error = addError(event.getLevel(), event.getMessage(),
                event.getThrowable(), event.getDetails(), isValidError(event.getThrowable()));
        if (error.getLevel() == ErrorLevel.FATAL) {
            eventBus.publish(new FatalProgramErrorEvent(error));
        } else {
            eventBus.publish(new NonFatalProgramErrorEvent(error));
        }
    }

    @Handler(priority = EventUtils.PRIORITY_LOWEST)
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    void handleProgramError(final NonFatalProgramErrorEvent event) {
        if (!event.isHandled()) {
            System.err.println(event.getError().getMessage());
            System.err.println(event.getError().getDetails());
            System.err.println(Throwables.getStackTraceAsString(event.getError().getThrowable()));
        }
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
                getTrace(message, throwable), details, new Date(), appError);
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
        final Collection<ProgramError> errorsCopy = new HashSet<>(errors);
        errors.clear();
        errorsCopy.stream().map(ProgramErrorDeletedEvent::new).forEach(eventBus::publish);
    }

    /**
     * Returns the list of program errors.
     *
     * @return Program error list
     */
    public Set<ProgramError> getErrors() {
        return Collections.unmodifiableSet(errors);
    }

    /**
     * Determines whether or not the specified exception is one that we are willing to report.
     *
     * @param exception The exception to test
     *
     * @since 0.6.3m1
     * @return True if the exception may be reported, false otherwise
     */
    private boolean isValidError(final Throwable exception) {
        // TODO: Dedupe this from here and SentryLoggingErrorManager
        Throwable target = exception;
        while (target != null) {
            for (Class<?> bad : BANNED_EXCEPTIONS) {
                if (bad.equals(target.getClass())) {
                    return false;
                }
            }
            target = target.getCause();
        }
        return true;
    }
}
