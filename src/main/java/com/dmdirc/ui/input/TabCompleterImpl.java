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

package com.dmdirc.ui.input;

import com.dmdirc.config.provider.AggregateConfigProvider;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * The tab completer handles a user's request to tab complete some word.
 */
public class TabCompleterImpl implements TabCompleter {

    /**
     * The parent TabCompleter. Results from parents are merged with results from this completer.
     */
    @Nullable
    private final TabCompleter parent;
    /** The config manager to use for reading settings. */
    private final AggregateConfigProvider configManager;
    /** The entries in this completer. */
    private final Multimap<TabCompletionType, String> entries = ArrayListMultimap.create();

    /**
     * Creates a new instance of {@link TabCompleterImpl}.
     *
     * @param configManager     The manager to read config settings from.
     */
    public TabCompleterImpl(final AggregateConfigProvider configManager) {
        this.parent = null;
        this.configManager = configManager;
    }

    /**
     * Creates a new instance of {@link TabCompleterImpl}.
     *
     * @param configManager     The manager to read config settings from.
     * @param parent            The parent tab completer to inherit completions from.
     */
    public TabCompleterImpl(
            final AggregateConfigProvider configManager,
            @Nullable final TabCompleter parent) {
        this.parent = parent;
        this.configManager = configManager;
    }

    @Override
    public List<String> complete(final String partial, @Nullable final AdditionalTabTargets additionals) {
        final List<String> result = new ArrayList<>();

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
                .forEach(result::add);

        if (parent != null) {
            if (additionals != null) {
                additionals.clear();
            }

            parent.complete(partial, additionals).stream()
                    .filter(match -> !result.contains(match))
                    .forEach(result::add);
        }

        return result;
    }

    @Override
    public void addEntry(final TabCompletionType type, final String entry) {
        entries.put(type, entry);
    }

    @Override
    public void addEntries(final TabCompletionType type, final Iterable<String> newEntries) {
        if (newEntries == null) {
            return;
        }

        for (String entry : newEntries) {
            addEntry(type, entry);
        }
    }

    @Override
    public void removeEntry(final TabCompletionType type, final String entry) {
        entries.remove(type, entry);
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public void clear(final TabCompletionType type) {
        entries.removeAll(type);
    }

}
