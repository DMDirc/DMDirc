/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

package com.dmdirc.commandparser.auto;

import com.google.common.eventbus.EventBus;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Manages {@link AutoCommand}s.
 */
@Singleton
public class AutoCommandManager {

    /** The bus to listen for events on. */
    private final EventBus eventBus;
    /** Known auto commands, mapped on to their handlers. */
    private final Map<AutoCommand, AutoCommandHandler> autoCommands = new ConcurrentSkipListMap<>();
    /** Whether the manager has been started or not. */
    private boolean started;

    /**
     * Creates a new instance of {@link AutoCommandManager}.
     *
     * @param eventBus The bus to listen to events on.
     */
    @Inject
    public AutoCommandManager(final EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Starts handling events and triggering auto commands.
     */
    public void start() {
        started = true;
        for (AutoCommandHandler handler : autoCommands.values()) {
            eventBus.register(handler);
        }
    }

    /**
     * Stops handling events and triggering auto commands.
     */
    public void stop() {
        started = false;
        for (AutoCommandHandler handler : autoCommands.values()) {
            eventBus.unregister(handler);
        }
    }

    /**
     * Adds an auto command to this manager.
     *
     * @param autoCommand The command to be added.
     */
    public void addAutoCommand(final AutoCommand autoCommand) {
        checkNotNull(autoCommand);
        final AutoCommandHandler handler = new AutoCommandHandler();

        if (started) {
            eventBus.register(handler);
        }

        autoCommands.put(autoCommand, handler);
    }

    /**
     * Removes an existing auto command from this manager.
     *
     * @param autoCommand The command to be removed.
     */
    public void removeAutoCommand(final AutoCommand autoCommand) {
        checkNotNull(autoCommand);
        final AutoCommandHandler handler = autoCommands.remove(autoCommand);

        if (started) {
            eventBus.unregister(handler);
        }
    }

    /**
     * Gets a set of all registered auto commands.
     *
     * @return The set of all known auto commands.
     */
    public Set<AutoCommand> getAutoCommands() {
        return Collections.unmodifiableSet(autoCommands.keySet());
    }

}
