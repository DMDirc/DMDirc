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

package com.dmdirc.commandparser.auto;

import com.dmdirc.DMDircMBassador;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Manages {@link AutoCommand}s.
 */
@Singleton
public class AutoCommandManager {

    /** The bus to listen for events on. */
    private final DMDircMBassador eventBus;
    /** The factory to use to create handlers. */
    private final AutoCommandHandlerFactory factory;
    /** Known auto commands, mapped on to their handlers. */
    private final Map<AutoCommand, AutoCommandHandler> autoCommands = new ConcurrentHashMap<>();
    /** Whether the manager has been started or not. */
    private boolean started;

    /**
     * Creates a new instance of {@link AutoCommandManager}.
     *
     * @param eventBus The bus to listen to events on.
     * @param factory  The factory to use to create handlers.
     */
    @Inject
    public AutoCommandManager(
            final DMDircMBassador eventBus,
            final AutoCommandHandlerFactory factory) {
        this.eventBus = eventBus;
        this.factory = factory;
    }

    /**
     * Starts handling events and triggering auto commands.
     */
    public void start() {
        started = true;
        autoCommands.values().forEach(eventBus::subscribe);
    }

    /**
     * Stops handling events and triggering auto commands.
     */
    public void stop() {
        started = false;
        autoCommands.values().forEach(eventBus::unsubscribe);
    }

    /**
     * Adds an auto command to this manager.
     *
     * <p>Only one auto command may exist for each combination of network, server and profile
     * targets. This method will throw an {@link IllegalStateException} if the given command is
     * a duplicate.
     *
     * @param autoCommand The command to be added.
     * @throws IllegalStateException If a command with the same target already exists.
     */
    public void addAutoCommand(final AutoCommand autoCommand) {
        checkNotNull(autoCommand);

        if (getAutoCommand(autoCommand.getNetwork(), autoCommand.getServer(),
                autoCommand.getProfile()).isPresent()) {
            throw new IllegalStateException("Only one AutoCommand may exist per " +
                    "network/server/profile");
        }

        final AutoCommandHandler handler = factory.getAutoCommandHandler(autoCommand);

        if (started) {
            eventBus.subscribe(handler);
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
            eventBus.unsubscribe(handler);
        }
    }

    /**
     * 'Replaces' one AutoCommand with another, by removing the original and adding the replacement.
     *
     * @param original The original command to be replaced.
     * @param replacement The new command to be added.
     */
    public void replaceAutoCommand(final AutoCommand original, final AutoCommand replacement) {
        removeAutoCommand(original);
        addAutoCommand(replacement);
    }

    /**
     * Gets a set of all registered auto commands.
     *
     * @return The set of all known auto commands.
     */
    public Set<AutoCommand> getAutoCommands() {
        return Collections.unmodifiableSet(autoCommands.keySet());
    }

    /**
     * Returns the single global auto command, if it exists.
     *
     * @return The global auto-command, if it exists.
     */
    public Optional<AutoCommand> getGlobalAutoCommand() {
        return getAutoCommand(Optional.empty(), Optional.empty(), Optional.empty());
    }

    /**
     * Returns a single auto command matching the given parameters, if one exists.
     *
     * @param network The network to match
     * @param server The server to match
     * @param profile The profile to match
     * @return The matched auto command, if it exists.
     */
    public Optional<AutoCommand> getAutoCommand(final Optional<String> network,
            final Optional<String> server, final Optional<String> profile) {
        return getAutoCommands()
                .parallelStream()
                .filter(c -> c.getNetwork().equals(network))
                .filter(c -> c.getServer().equals(server))
                .filter(c -> c.getProfile().equals(profile))
                .findAny();
    }

    /**
     * Returns a single auto command matching the given parameters, or creates a new one if it
     * doesn't exist.
     *
     * @param network The network to match
     * @param server The server to match
     * @param profile The profile to match
     * @return The matched auto command, or a new auto command with the given targets.
     */
    public AutoCommand getOrCreateAutoCommand(final Optional<String> network,
            final Optional<String> server, final Optional<String> profile) {
        return getAutoCommand(network, server, profile).orElse(
                AutoCommand.create(server, network, profile, ""));
    }

}
