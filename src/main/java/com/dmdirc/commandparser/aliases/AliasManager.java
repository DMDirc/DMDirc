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

package com.dmdirc.commandparser.aliases;

import com.dmdirc.interfaces.CommandController;

import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Maintains a list of known aliases, and registers and unregisters them with the command system.
 */
@Singleton
public class AliasManager {

    /** The controller to register aliases with. */
    private final CommandController commandController;
    /** Map of known alias names to their corresponding aliases. */
    private final Map<String, Alias> aliases = new ConcurrentSkipListMap<>();
    /** Whether or not changes have been made compared to the stored version. */
    private boolean dirty;

    @Inject
    public AliasManager(final CommandController commandController) {
        this.commandController = commandController;
    }

    /**
     * Adds a new alias and registers it with the command system.
     * <p>
     * If an existing alias with the same name already exists, it is removed.
     *
     * @param alias The alias to be registered
     */
    public void addAlias(final Alias alias) {
        if (aliases.containsKey(alias.getName())) {
            removeAlias(alias);
        }

        aliases.put(alias.getName(), alias);
        commandController.registerCommand(new AliasCommandHandler(commandController, alias), alias);
        dirty = true;
    }

    /**
     * Removes any existing alias with the same name, and unregisters it with the command system.
     * <p>
     * If the alias was not previously added, no action occurs.
     *
     * @param alias The alias to be removed
     */
    public void removeAlias(final Alias alias) {
        if (aliases.containsKey(alias.getName())) {
            commandController.unregisterCommand(aliases.remove(alias.getName()));
            dirty = true;
        }
    }

    /**
     * Removes an existing alias, and unregisters it with the command system.
     * <p>
     * If the alias was not previously added, no action occurs.
     *
     * @param name The name of the alias to be removed
     */
    public void removeAlias(final String name) {
        if (aliases.containsKey(name)) {
            removeAlias(aliases.get(name));
            dirty = true;
        }
    }

    /**
     * Retrieves the set of all known alias names.
     *
     * @return Set of all known names.
     */
    public Set<String> getAliasNames() {
        return Collections.unmodifiableSet(aliases.keySet());
    }

    /**
     * Retrieves the set of all known aliases.
     *
     * @return Set of all known aliases.
     */
    public Set<Alias> getAliases() {
        return Collections.unmodifiableSet(Sets.newHashSet(aliases.values()));
    }

    /**
     * Retrieves the alias with the given name.
     *
     * @param name The name of the alias to receive.
     *
     * @return The alias, if found.
     */
    public Optional<Alias> getAlias(final String name) {
        return Optional.ofNullable(aliases.get(name));
    }

    /**
     * Gets the dirty state of the manager.
     *
     * @return True if the manager is dirty with respect to the store, false otherwise.
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Manually sets the dirty state of the manager. This should be done after the manager's
     * aliases are loaded or saved.
     *
     * @param dirty True if the manager is now dirty with respect to the store, false otherwise.
     */
    public void setDirty(final boolean dirty) {
        this.dirty = dirty;
    }
}
