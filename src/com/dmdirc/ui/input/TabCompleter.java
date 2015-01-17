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

package com.dmdirc.ui.input;

import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.config.AggregateConfigProvider;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import javax.annotation.Nullable;

/**
 * The tab completer handles a user's request to tab complete some word.
 */
public class TabCompleter {

    /**
     * The parent TabCompleter. Results from parents are merged with results from this completer.
     */
    @Nullable
    private final TabCompleter parent;
    /** The config manager to use for reading settings. */
    private final AggregateConfigProvider configManager;
    /** The controller to use to retrieve command information. */
    private final CommandController commandController;
    /** The entries in this completer. */
    private final Multimap<TabCompletionType, String> entries = ArrayListMultimap.create();

    /**
     * Creates a new instance of {@link TabCompleter}.
     *
     * @param commandController The controller to use for command information.
     * @param configManager     The manager to read config settings from.
     */
    public TabCompleter(
            final CommandController commandController,
            final AggregateConfigProvider configManager) {
        this.parent = null;
        this.commandController = commandController;
        this.configManager = configManager;
    }

    /**
     * Creates a new instance of {@link TabCompleter}.
     *
     * @param commandController The controller to use for command information.
     * @param configManager     The manager to read config settings from.
     * @param parent            The parent tab completer to inherit completions from.
     */
    public TabCompleter(
            final CommandController commandController,
            final AggregateConfigProvider configManager,
            @Nullable final TabCompleter parent) {
        this.parent = parent;
        this.commandController = commandController;
        this.configManager = configManager;
    }

    /**
     * Attempts to complete the partial string.
     *
     * @param partial     The string to tab complete
     * @param additionals A list of additional strings to use
     *
     * @return A TabCompleterResult containing any matches found
     */
    public TabCompletionMatches complete(final String partial,
            @Nullable final AdditionalTabTargets additionals) {
        final TabCompletionMatches result = new TabCompletionMatches();

        final boolean caseSensitive = configManager.getOptionBool("tabcompletion", "casesensitive");
        final boolean allowEmpty = configManager.getOptionBool("tabcompletion", "allowempty");

        if (partial.isEmpty() && !allowEmpty) {
            return result;
        }

        final Multimap<TabCompletionType, String> targets = ArrayListMultimap.create(entries);
        if (additionals != null) {
            targets.putAll(TabCompletionType.ADDITIONAL, additionals);
        }

        targets.keys().stream()
                // Filter out keys that aren't allowed by the additional argument (if present)
                .filter(k -> additionals == null || additionals.shouldInclude(k))
                // Select all values for the valid keys
                .flatMap(k -> targets.get(k).stream())
                // Filter out values that don't case sensitively match, if case sensitivity is on
                .filter(v -> !caseSensitive || v.startsWith(partial))
                // Filter out values that don't case INsensitively match, if case sensitivity is off
                .filter(v -> caseSensitive || v.toLowerCase().startsWith(partial.toLowerCase()))
                // Filter out duplicates
                .distinct()
                // Add them all to the result
                .forEach(result::addResult);

        if (parent != null) {
            if (additionals != null) {
                additionals.clear();
            }

            result.merge(parent.complete(partial, additionals));
        }

        return result;
    }

    /**
     * Adds a new entry to this tab completer's list.
     *
     * @param type  The type of the entry that's being added
     * @param entry The new entry to be added
     */
    public void addEntry(final TabCompletionType type, final String entry) {
        entries.put(type, entry);

        if (type == TabCompletionType.COMMAND && entry.length() > 1
                && entry.charAt(0) == commandController.getCommandChar()
                && entry.charAt(1) != commandController.getSilenceChar()) {
            // If we're adding a command name that doesn't include the silence
            // character, also add a version with the silence char
            addEntry(type, entry.substring(0, 1) + commandController.getSilenceChar()
                    + entry.substring(1));
        }
    }

    /**
     * Adds multiple new entries to this tab completer's list.
     *
     * @param type       The type of the entries that're being added
     * @param newEntries Entries to be added
     */
    public void addEntries(final TabCompletionType type, final Iterable<String> newEntries) {
        if (newEntries == null) {
            return;
        }

        for (String entry : newEntries) {
            addEntry(type, entry);
        }
    }

    /**
     * Removes a specified entry from this tab completer's list.
     *
     * @param type  The type of the entry that should be removed
     * @param entry The entry to be removed
     */
    public void removeEntry(final TabCompletionType type, final String entry) {
        entries.remove(type, entry);
    }

    /**
     * Clears all entries in this tab completer.
     */
    public void clear() {
        entries.clear();
    }

    /**
     * Clears all entries of the specified type in this tab completer.
     *
     * @param type The type of entry to clear
     */
    public void clear(final TabCompletionType type) {
        entries.removeAll(type);
    }

}
