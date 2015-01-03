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
import com.dmdirc.events.ErrorEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import net.engio.mbassy.listener.Handler;

/**
 * Listens for {@link ErrorEvent}s and writes them to disk.
 */
public class DiskLoggingErrorManager {

    /** The event bus to listen for errors on. */
    private final DMDircMBassador eventBus;
    /** The directory to log errors to. */
    private final Path errorsDirectory;
    /** The config binder to use for settings. */
    private final ConfigBinder configBinder;
    /** Error creating directory, don't write to disk. */
    private boolean directoryError;
    /** Are we logging errors to disk? */
    private boolean logging;

    /**
     * Creates a new instance of this error manager.
     *
     * @param errorsDirectory The directory to write errors to.  The error manager will try to
     *                        create this if it is not present
     * @param eventBus        The event bus to listen to errors on
     * @param config          The config to read values from
     */
    public DiskLoggingErrorManager(final Path errorsDirectory, final DMDircMBassador eventBus,
            final AggregateConfigProvider config) {
        this.errorsDirectory = errorsDirectory;
        this.eventBus = eventBus;
        configBinder = config.getBinder();
    }

    /**
     * Initialises the error manager.  Must be called before logging will start.
     */
    public void initialise() {
        configBinder.bind(this, DiskLoggingErrorManager.class);
        eventBus.subscribe(this);
        if (!Files.exists(errorsDirectory)) {
            try {
                Files.createDirectories(errorsDirectory);
            } catch (IOException ex) {
                directoryError = true;
            }
        }
    }

    /**
     * This is true is there was an error creating the error directory.  If this is true the logger
     * will not attempt to write to disk irrespective of the config setting.
     *
     * @return true if there was an error creating the error directory
     */
    public boolean isDirectoryError() {
        return directoryError;
    }

    @Handler
    void handleErrorEvent(final ErrorEvent appError) {
        saveError(appError);
    }

    @ConfigBinding(domain = "general", key = "logerrors")
    void handleLoggingSetting(final boolean value) {
        logging = value;
    }

    private void saveError(final ErrorEvent error) {
        if (directoryError || !logging) {
            return;
        }
        final String logName = error.getTimestamp() + "-" + error.getLevel();
        final Path errorFile = errorsDirectory.resolve(logName + ".log");
        final List<String> data = Lists
                .newArrayList("Date: " + new Date(error.getTimestamp()),
                        "Level: " + error.getLevel(),
                        "Description: " + error.getMessage(),
                        "Details: ");
        Arrays.stream(Throwables.getStackTraceAsString(error.getThrowable()).split("\n"))
                .forEach(data::add);
        try {
            Files.write(errorFile, data, Charset.forName("UTF-8"));
        } catch (IOException ex) {
            //Not really anything we can do at this point, so don't try.
        }
    }
}
