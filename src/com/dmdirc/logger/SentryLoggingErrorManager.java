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
import com.dmdirc.config.ConfigBinder;
import com.dmdirc.config.ConfigBinding;
import com.dmdirc.events.ProgramErrorAddedEvent;
import com.dmdirc.events.ProgramErrorEvent;
import com.dmdirc.events.ProgramErrorStatusEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import net.engio.mbassy.listener.Handler;
import net.kencochrane.raven.DefaultRavenFactory;
import net.kencochrane.raven.RavenFactory;

/**
 * Listens for {@link ProgramErrorEvent}s and reports these to Sentry.
 */
@Singleton
public class SentryLoggingErrorManager {

    /** A list of exceptions which we don't consider bugs and thus don't report. */
    private static final Class<?>[] BANNED_EXCEPTIONS = new Class<?>[]{
            NoSuchMethodError.class, NoClassDefFoundError.class,
            UnsatisfiedLinkError.class, AbstractMethodError.class,
            IllegalAccessError.class, OutOfMemoryError.class,
            NoSuchFieldError.class,};
    /** The event bus to listen for errors on. */
    private final DMDircMBassador eventBus;
    /** Sentry error reporter factory. */
    private final SentryErrorReporter sentryErrorReporter;
    /** Thread used for sending errors. */
    private final ExecutorService executorService;
    /** Whether to submit error reports. */
    private boolean submitReports;
    /** Temp no error reporting. */
    private boolean tempNoErrors;
    /** Whether or not to send error reports. */
    private boolean sendReports;

    @Inject
    public SentryLoggingErrorManager(final DMDircMBassador eventBus,
            final SentryErrorReporter sentryErrorReporter,
            @Named("singlethread") final ExecutorService executorService) {
        this.eventBus = eventBus;
        this.sentryErrorReporter = sentryErrorReporter;
        this.executorService = executorService;
    }

    /**
     * Initialises the error manager.  Must be called before logging will start.
     */
    public void initialise(final AggregateConfigProvider config) {
        RavenFactory.registerFactory(new DefaultRavenFactory());
        final ConfigBinder configBinder = config.getBinder();
        configBinder.bind(this, SentryLoggingErrorManager.class);
        eventBus.subscribe(this);
    }

    @Handler
    void handleErrorEvent(final ProgramErrorAddedEvent error) {
        final boolean appError = error.getError().isAppError();
        if (!isValidError(error.getError().getThrowable())
                || !isValidSource(error.getError().getTrace())
                || !appError) {
            error.getError().setReportStatus(ErrorReportStatus.NOT_APPLICABLE);
            eventBus.publish(new ProgramErrorStatusEvent(error.getError()));
        } else if (sendReports) {
            sendError(error.getError());
        }
    }

    void sendError(final ProgramError error) {
        executorService.submit(new ErrorReportingRunnable(sentryErrorReporter, error));
    }

    @ConfigBinding(domain = "general", key = "submitErrors")
    void handleSubmitErrors(final boolean value) {
        submitReports = value;
        sendReports = submitReports && !tempNoErrors;
    }

    @ConfigBinding(domain = "temp", key="noerrorreporting")
    void handleNoErrorReporting(final boolean value) {
        tempNoErrors = value;
        sendReports = submitReports && !tempNoErrors;
    }

    /**
     * Determines whether or not the stack trace associated with this error is from a valid source.
     * A valid source is one that is within a DMDirc package (com.dmdirc), and is not the DMDirc
     * event queue.
     *
     * @return True if the source is valid, false otherwise
     */
    private boolean isValidSource(final Collection<String> trace) {
        final String line = getSourceLine(trace).orElse("").trim();
        return line.startsWith("at com.dmdirc")
                && !line.startsWith("at com.dmdirc.addons.ui_swing.DMDircEventQueue");
    }

    /**
     * Returns the "source line" of this error, which is defined as the first line starting with a
     * DMDirc package name (com.dmdirc). If no such line is found, returns the first line of the
     * message.
     *
     * @return This error's source line
     */
    private Optional<String> getSourceLine(final Collection<String> trace) {
        for (String line : trace) {
            if (line.trim().startsWith("at com.dmdirc")) {
                return Optional.of(line);
            }
        }
        return trace.stream().findFirst();
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
