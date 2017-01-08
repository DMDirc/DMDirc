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

import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.config.binding.ConfigBinder;
import com.dmdirc.config.binding.ConfigBinding;
import com.dmdirc.events.ErrorEvent;
import com.dmdirc.events.ProgramErrorEvent;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.config.AggregateConfigProvider;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.engio.mbassy.listener.Handler;

/**
 * Listens for {@link ErrorEvent}s and writes them to disk.
 */
@Singleton
public class DiskLoggingErrorManager {

    /** The event bus to listen for errors on. */
    private final EventBus eventBus;
    /** The directory to log errors to. */
    private final Path errorsDirectory;
    /** Error creating directory, don't write to disk. */
    private boolean directoryError;
    /** Are we logging errors to disk? */
    private boolean logging;

    @Inject
    public DiskLoggingErrorManager(
            @Directory(DirectoryType.ERRORS) final Path errorsDirectory,
            final EventBus eventBus) {
        this.errorsDirectory = errorsDirectory;
        this.eventBus = eventBus;
    }

    /**
     * Initialises the error manager.  Must be called before logging will start.
     */
    public void initialise(final AggregateConfigProvider config) {
        final ConfigBinder configBinder = config.getBinder();
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
    void handleErrorEvent(final ProgramErrorEvent error) {
        if (directoryError || !logging) {
            return;
        }
        final String logName = error.getTimestamp() + "-" + error.getError().getLevel();
        final Path errorFile = errorsDirectory.resolve(logName + ".log");
        final List<String> data = Lists
                .newArrayList("Date: " + error.getTimestamp(),
                        "Level: " + error.getError().getLevel(),
                        "Description: " + error.getError().getMessage(),
                        "Details: ");
        error.getError().getThrowableAsString()
                .ifPresent(s -> Arrays.stream(s.split("\n")).forEach(data::add));
        try {
            Files.write(errorFile, data, Charset.forName("UTF-8"));
        } catch (IOException ex) {
            //Not really anything we can do at this point, so don't try.
        }
    }

    @ConfigBinding(domain = "general", key = "logerrors")
    void handleLoggingSetting(final boolean value) {
        logging = value;
    }
}
