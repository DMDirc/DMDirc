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
import com.dmdirc.config.ConfigBinding;
import com.dmdirc.events.AppErrorEvent;
import com.dmdirc.events.FatalProgramErrorEvent;
import com.dmdirc.events.NonFatalProgramErrorEvent;
import com.dmdirc.events.ProgramErrorDeletedEvent;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.FatalErrorDialog;
import com.dmdirc.util.EventUtils;
import com.dmdirc.util.collections.ListenerList;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.engio.mbassy.listener.Handler;
import net.kencochrane.raven.DefaultRavenFactory;
import net.kencochrane.raven.RavenFactory;

/**
 * Error manager.
 */
@Singleton
public class ErrorManager {

    /** A list of exceptions which we don't consider bugs and thus don't report. */
    private static final Class<?>[] BANNED_EXCEPTIONS = new Class<?>[]{
        NoSuchMethodError.class, NoClassDefFoundError.class,
        UnsatisfiedLinkError.class, AbstractMethodError.class,
        IllegalAccessError.class, OutOfMemoryError.class,
        NoSuchFieldError.class,};
    /** Error list. */
    private final List<ProgramError> errors;
    /** Listener list. */
    private final ListenerList errorListeners = new ListenerList();
    /** Countdown latch to wait for FED with. */
    private final CountDownLatch countDownLatch;
    /** Sentry error reporter factory. */
    private final SentryErrorReporter sentryErrorReporter;
    /** Factory to create program errors. */
    private final ProgramErrorFactory programErrorFactory;
    /** Event bus to subscribe and publish errors on. */
    private DMDircMBassador eventBus;
    /** Whether or not to send error reports. */
    private boolean sendReports;
    /** Whether or not to log error reports. */
    private boolean logReports;
    /** Whether to submit error reports. */
    private boolean submitReports;
    /** Temp no error reporting. */
    private boolean tempNoErrors;
    /** Error creating directory, don't write to disk. */
    private boolean directoryError;
    /** Thread used for sending errors. */
    private ExecutorService reportThread;
    /** Directory to store errors in. */
    private Path errorsDirectory;

    /** Creates a new instance of ErrorListDialog. */
    @Inject
    public ErrorManager(final SentryErrorReporter sentryErrorReporter,
            final ProgramErrorFactory programErrorFactory) {
        errors = new LinkedList<>();
        countDownLatch = new CountDownLatch(2);
        this.sentryErrorReporter = sentryErrorReporter;
        this.programErrorFactory = programErrorFactory;
    }

    /**
     * Initialises the error manager.
     *
     * @param globalConfig The configuration to read settings from.
     * @param directory    The directory to store errors in, if enabled.
     * @param eventBus     The event bus to listen for error events on.
     */
    public void initialise(final AggregateConfigProvider globalConfig, final Path directory,
            final DMDircMBassador eventBus) {
        this.eventBus = eventBus;
        eventBus.subscribe(this);
        RavenFactory.registerFactory(new DefaultRavenFactory());
        reportThread = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setNameFormat("error-thread-%d").build());

        globalConfig.getBinder().bind(this, ErrorManager.class);

        errorsDirectory = directory;
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(errorsDirectory);
            } catch (IOException ex) {
                directoryError = true;
            }
        }

        // Loop through any existing errors and send/save them per the config.
        for (ProgramError error : errors) {
            if (sendReports && error.getReportStatus() == ErrorReportStatus.WAITING) {
                sendError(error);
            }

            if (logReports) {
                saveError(error);
            }
        }
    }

    @Handler(priority = EventUtils.PRIORITY_LOWEST)
    public void handleAppErrorEvent(final AppErrorEvent appError) {
        final ProgramError error = addError(appError.getLevel(), appError.getMessage(), appError
                        .getThrowable(),
                appError.getDetails(), true, isValidError(appError.getThrowable()));
        if (appError.getLevel() == ErrorLevel.FATAL) {
            eventBus.publishAsync(new FatalProgramErrorEvent(error));
        } else {
            eventBus.publishAsync(new NonFatalProgramErrorEvent(error));
        }
    }

    @Handler(priority = EventUtils.PRIORITY_LOWEST)
    public void handleUserErrorEvent(final UserErrorEvent userError) {
        final ProgramError error = addError(userError.getLevel(), userError.getMessage(),
                userError.getThrowable(), userError.getDetails(), false,
                isValidError(userError.getThrowable()));
        if (userError.getLevel() == ErrorLevel.FATAL) {
            eventBus.publishAsync(new FatalProgramErrorEvent(error));
        } else {
            eventBus.publishAsync(new NonFatalProgramErrorEvent(error));
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
     * @param canReport Whether or not this error can be reported
     *
     * @since 0.6.3m1
     */
    protected ProgramError addError(final ErrorLevel level, final String message,
            final Throwable throwable, final String details, final boolean appError,
            final boolean canReport) {
        final ProgramError error = programErrorFactory.create(level, message, throwable,
                getTrace(message, throwable), details, new Date(), this, appError);
        return addError(error, appError, canReport);
    }

    protected ProgramError addError(
            final ProgramError error,
            final boolean appError,
            final boolean canReport) {
        addError(error);
        if (!canReport || appError && !isValidSource(error) || !appError) {
            error.setReportStatus(ErrorReportStatus.NOT_APPLICABLE);
        } else if (sendReports) {
            sendError(error);
        }

        if (logReports) {
            saveError(error);
        }
        return error;
    }



    /**
     * Determines whether or not the stack trace associated with this error is from a valid source.
     * A valid source is one that is within a DMDirc package (com.dmdirc), and is not the DMDirc
     * event queue.
     *
     * @return True if the source is valid, false otherwise
     */
    public boolean isValidSource(final ProgramError error) {
        final String line = getSourceLine(error);

        return line.startsWith("com.dmdirc")
                && !line.startsWith("com.dmdirc.addons.ui_swing.DMDircEventQueue");
    }

    /**
     * Returns the "source line" of this error, which is defined as the first line starting with a
     * DMDirc package name (com.dmdirc). If no such line is found, returns the first line of the
     * message.
     *
     * @return This error's source line
     */
    public String getSourceLine(final ProgramError error) {
        for (String line : error.getTrace()) {
            if (line.startsWith("com.dmdirc")) {
                return line;
            }
        }

        return error.getTrace().get(0);
    }

    /**
     * Adds the specified error to the list of known errors and determines if it was previously
     * added.
     *
     * @param error The error to be added
     */
    protected void addError(final ProgramError error) {
        synchronized (errors) {
            errors.add(error);
        }
    }

    /**
     * Determines whether or not the specified exception is one that we are willing to report.
     *
     * @param exception The exception to test
     *
     * @since 0.6.3m1
     * @return True if the exception may be reported, false otherwise
     */
    protected boolean isValidError(final Throwable exception) {
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

    /**
     * Sends an error to the developers.
     *
     * @param error error to be sent
     */
    public void sendError(final ProgramError error) {
        if (error.getReportStatus() != ErrorReportStatus.ERROR
                && error.getReportStatus() != ErrorReportStatus.WAITING) {
            return;
        }
        error.setReportStatus(ErrorReportStatus.QUEUED);

        reportThread.submit(new ErrorReportingRunnable(sentryErrorReporter, error));
    }

    /**
     * Called when an error needs to be deleted from the list.
     *
     * @param error ProgramError that changed
     */
    public void deleteError(final ProgramError error) {
        synchronized (errors) {
            errors.remove(error);
        }

        eventBus.publishAsync(new ProgramErrorDeletedEvent(error));
        fireErrorDeleted(error);
    }

    /**
     * Deletes all errors from the manager.
     *
     * @since 0.6.3m1
     */
    public void deleteAll() {
        synchronized (errors) {
            errors.forEach(e -> {
                fireErrorDeleted(e);
                eventBus.publishAsync(new ProgramErrorDeletedEvent(e));
            });
            errors.clear();
        }
    }

    /**
     * Returns the number of errors.
     *
     * @return Number of ProgramErrors
     */
    public int getErrorCount() {
        return errors.size();
    }

    /**
     * Returns the list of program errors.
     *
     * @return Program error list
     */
    public List<ProgramError> getErrors() {
        synchronized (errors) {
            return new LinkedList<>(errors);
        }
    }

    /**
     * Adds an ErrorListener to the listener list.
     *
     * @param listener Listener to add
     */
    public void addErrorListener(final ErrorListener listener) {
        if (listener == null) {
            return;
        }

        errorListeners.add(ErrorListener.class, listener);
    }

    /**
     * Removes an ErrorListener from the listener list.
     *
     * @param listener Listener to remove
     */
    public void removeErrorListener(final ErrorListener listener) {
        errorListeners.remove(ErrorListener.class, listener);
    }

    /**
     * Fired when the program encounters an error.
     *
     * @param event Error that occurred
     */
    @Handler(priority = EventUtils.PRIORITY_LOWEST)
    protected void fireErrorAdded(final NonFatalProgramErrorEvent event) {
        // TODO: Make UI listen for the event and remove this
        errorListeners.get(ErrorListener.class).stream().filter(ErrorListener::isReady)
                .forEach(listener -> {
                    event.setHandled();
                    listener.errorAdded(event.getError());
                });
        if (!event.isHandled()) {
            System.err.println("An error has occurred: " + event.getError().getLevel() + ": "
                            + event.getError().getMessage());

            for (String line : event.getError().getTrace()) {
                System.err.println("\t" + line);
            }
        }
    }

    /**
     * Fired when the program encounters a fatal error.
     *
     * @param event Error that occurred
     */
    @Handler(priority = EventUtils.PRIORITY_LOWEST)
    protected void fireFatalError(final FatalProgramErrorEvent event) {
        final boolean restart;
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("A fatal error has occurred: " + event.getError().getMessage());
            for (String line : event.getError().getTrace()) {
                System.err.println("\t" + line);
            }
            restart = false;
        } else {
            final FatalErrorDialog fed = new FatalErrorDialog(event.getError(), this,
                    countDownLatch, sendReports);
            fed.setVisible(true);
            try {
                countDownLatch.await();
            } catch (InterruptedException ex) {
                //Nevermind, carry on
            }
            restart = fed.getRestart();
        }

        if (restart) {
            System.exit(42);
        } else {
            System.exit(1);
        }
    }

    /**
     * Fired when an error is deleted.
     *
     * @param error Error that has been deleted
     */
    protected void fireErrorDeleted(final ProgramError error) {
        errorListeners.get(ErrorListener.class).forEach(l -> l.errorDeleted(error));
    }

    /**
     * Fired when an error's status is changed.
     *
     * @param error Error that has been altered
     */
    protected void fireErrorStatusChanged(final ProgramError error) {
        errorListeners.get(ErrorListener.class).forEach(l -> l.errorStatusChanged(error));
    }

    @ConfigBinding(domain = "general", key = "submitErrors")
    public void handleSubmitErrors(final boolean value) {
        submitReports = value;

        sendReports = submitReports && !tempNoErrors;
    }

    @ConfigBinding(domain = "general", key="logerrors")
    public void handleLogErrors(final boolean value) {
        logReports = value;
    }

    @ConfigBinding(domain = "temp", key="noerrorreporting")
    public void handleNoErrorReporting(final boolean value) {
        tempNoErrors = value;
        sendReports = submitReports && !tempNoErrors;
    }

    /**
     * Returns this errors trace.
     *
     * @return Error trace
     */
    public List<String> getTrace(final String message, final Throwable exception) {
        return Arrays.asList(exception == null ? message == null ? new String[0]
                : new String[]{message} : getTrace(exception));
    }

    /**
     * Converts an exception into a string array.
     *
     * @param throwable Exception to convert
     *
     * @since 0.6.3m1
     * @return Exception string array
     */
    private static String[] getTrace(final Throwable throwable) {
        String[] trace;

        if (throwable == null) {
            trace = new String[0];
        } else {
            final StackTraceElement[] traceElements = throwable.getStackTrace();
            trace = new String[traceElements.length + 1];

            trace[0] = throwable.toString();

            for (int i = 0; i < traceElements.length; i++) {
                trace[i + 1] = traceElements[i].toString();
            }

            if (throwable.getCause() != null) {
                final String[] causeTrace = getTrace(throwable.getCause());
                final String[] newTrace = new String[trace.length + causeTrace.length];
                trace[0] = "\nWhich caused: " + trace[0];

                System.arraycopy(causeTrace, 0, newTrace, 0, causeTrace.length);
                System.arraycopy(trace, 0, newTrace, causeTrace.length, trace.length);

                trace = newTrace;
            }
        }

        return trace;
    }

    private void saveError(final ProgramError error) {
        if (directoryError) {
            return;
        }
        final String logName = error.getDate().getTime() + "-" + error.getLevel();
        final Path errorFile = errorsDirectory.resolve(logName + ".log");
        final List<String> data = Lists.newArrayList("Date: " + error.getDate(),
                "Level: " + error.getLevel(),
                "Description: " + error.getMessage(),
                "Details: ");
        error.getTrace().forEach(line -> data.add('\t' + line));
        try {
            Files.write(errorFile, data, Charset.forName("UTF-8"));
        } catch (IOException ex) {
            //Not really anything we can do at this point, so don't try.
        }
    }
}
