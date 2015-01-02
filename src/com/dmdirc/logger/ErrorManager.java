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
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.FatalErrorDialog;
import com.dmdirc.util.ClientInfo;
import com.dmdirc.util.collections.ListenerList;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.awt.GraphicsEnvironment;
import java.nio.file.Path;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

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
    /** Whether or not to send error reports. */
    private boolean sendReports;
    /** Whether or not to log error reports. */
    private boolean logReports;
    /** Whether to submit error reports. */
    private boolean submitReports;
    /** Temp no error reporting. */
    private boolean tempNoErrors;
    /** Error list. */
    private final List<ProgramError> errors;
    /** Listener list. */
    private final ListenerList errorListeners = new ListenerList();
    /** Next error ID. */
    private final AtomicLong nextErrorID;
    /** Thread used for sending errors. */
    private ExecutorService reportThread;
    /** Directory to store errors in. */
    private Path errorsDirectory;
    private ClientInfo clientInfo;

    /** Creates a new instance of ErrorListDialog. */
    @Inject
    public ErrorManager() {
        errors = new LinkedList<>();
        nextErrorID = new AtomicLong();
    }

    /**
     * Initialises the error manager.
     *
     * @param globalConfig The configuration to read settings from.
     * @param directory    The directory to store errors in, if enabled.
     * @param eventBus     The event bus to listen for error events on.
     */
    public void initialise(final AggregateConfigProvider globalConfig, final Path directory,
            final DMDircMBassador eventBus, final ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
        eventBus.subscribe(this);
        RavenFactory.registerFactory(new DefaultRavenFactory());
        reportThread = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setNameFormat("error-thread-%d").build());

        globalConfig.getBinder().bind(this, ErrorManager.class);

        errorsDirectory = directory;

        // Loop through any existing errors and send/save them per the config.
        for (ProgramError error : errors) {
            if (sendReports && error.getReportStatus() == ErrorReportStatus.WAITING) {
                sendError(error);
            }

            if (logReports) {
                error.save(errorsDirectory);
            }
        }
    }

    @Handler
    public void handleAppErrorEvent(final AppErrorEvent appError) {
        addError(appError.getLevel(), appError.getMessage(), appError.getThrowable(),
                appError.getDetails(), true, isValidError(appError.getThrowable()));
    }

    @Handler
    public void handleUserErrorEvent(final UserErrorEvent userError) {
        addError(userError.getLevel(), userError.getMessage(), userError.getThrowable(),
                userError.getDetails(), false, isValidError(userError.getThrowable()));
    }

    /**
     * Adds a new error to the manager with the specified details.
     *
     * @param level     The severity of the error
     * @param message   The error message
     * @param exception The exception that caused the error, if any.
     * @param details   The details of the exception, if any.
     * @param appError  Whether or not this is an application error
     * @param canReport Whether or not this error can be reported
     *
     * @since 0.6.3m1
     */
    protected void addError(final ErrorLevel level, final String message,
            final Throwable exception, final String details, final boolean appError,
            final boolean canReport) {
        addError(getError(level, message, exception, details), appError, canReport);
    }

    protected void addError(
            final ProgramError error,
            final boolean appError,
            final boolean canReport) {
        final boolean dupe = addError(error);
        if (error.getLevel() == ErrorLevel.FATAL) {
            if (dupe) {
                error.setReportStatus(ErrorReportStatus.NOT_APPLICABLE);
            }
        } else if (!canReport || appError && !error.isValidSource() || !appError || dupe) {
            error.setReportStatus(ErrorReportStatus.NOT_APPLICABLE);
        } else if (sendReports) {
            sendError(error);
        }

        if (logReports) {
            error.save(errorsDirectory);
        }

        if (!dupe) {
            if (error.getLevel() == ErrorLevel.FATAL) {
                fireFatalError(error);
            } else {
                fireErrorAdded(error);
            }
        }
    }

    /**
     * Adds the specified error to the list of known errors and determines if it was previously
     * added.
     *
     * @param error The error to be added
     *
     * @return True if a duplicate error has already been registered, false otherwise
     */
    protected boolean addError(final ProgramError error) {
        final int index;

        synchronized (errors) {
            index = errors.indexOf(error);
            if (index == -1) {
                errors.add(error);
            } else {
                errors.get(index).updateLastDate();
            }
        }

        return index > -1;
    }

    /**
     * Retrieves a {@link ProgramError} that represents the specified details.
     *
     * @param level     The severity of the error
     * @param message   The error message
     * @param exception The exception that caused the error.
     * @param details   The details of the exception
     *
     * @since 0.6.3m1
     * @return A corresponding ProgramError
     */
    protected ProgramError getError(final ErrorLevel level, final String message,
            final Throwable exception, final String details) {
        return new ProgramError(nextErrorID.getAndIncrement(), level, message, exception,
                details, new Date(), clientInfo, this);
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

        reportThread.submit(new ErrorReportingThread(error));
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

        fireErrorDeleted(error);
    }

    /**
     * Deletes all errors from the manager.
     *
     * @since 0.6.3m1
     */
    public void deleteAll() {
        synchronized (errors) {
            errors.forEach(this::fireErrorDeleted);
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
     * @param error Error that occurred
     */
    protected void fireErrorAdded(final ProgramError error) {
        errorListeners.get(ErrorListener.class).stream().filter(ErrorListener::isReady)
                .forEach(listener -> {
                    error.setHandled();
                    listener.errorAdded(error);
                });

        if (!error.isHandled()) {
            System.err.println(
                    "An error has occurred: " + error.getLevel() + ": " + error.getMessage());

            for (String line : error.getTrace()) {
                System.err.println("\t" + line);
            }
        }
    }

    /**
     * Fired when the program encounters a fatal error.
     *
     * @param error Error that occurred
     */
    protected void fireFatalError(final ProgramError error) {
        final boolean restart;
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("A fatal error has occurred: " + error.getMessage());
            for (String line : error.getTrace()) {
                System.err.println("\t" + line);
            }
            restart = false;
        } else {
            final FatalErrorDialog fed = new FatalErrorDialog(error, this);
            fed.setVisible(true);
            try {
                synchronized (fed) {
                    while (fed.isWaiting()) {
                        fed.wait();
                    }
                }
            } catch (InterruptedException ex) {
                //Oh well, carry on
            }
            restart = fed.getRestart();
        }

        try {
            synchronized (error) {
                while (!error.getReportStatus().isTerminal()) {
                    error.wait();
                }
            }
        } catch (InterruptedException ex) {
            // Do nothing
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
}
