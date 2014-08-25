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

package com.dmdirc.ui.input;

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.IntelligentCommand.IntelligentCommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.util.collections.MapList;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private final MapList<TabCompletionType, String> entries = new MapList<>();

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
    public TabCompleterResult complete(final String partial,
            final AdditionalTabTargets additionals) {
        final TabCompleterResult result = new TabCompleterResult(configManager);

        final MapList<TabCompletionType, String> targets = new MapList<>(entries);

        final boolean caseSensitive = configManager.getOptionBool("tabcompletion", "casesensitive");
        final boolean allowEmpty = configManager.getOptionBool("tabcompletion", "allowempty");

        if (partial.isEmpty() && !allowEmpty) {
            return result;
        }

        if (additionals != null) {
            targets.safeGet(TabCompletionType.ADDITIONAL).addAll(additionals);
        }

        for (Map.Entry<TabCompletionType, List<String>> typeEntry : targets.entrySet()) {
            if (additionals != null && !additionals.shouldInclude(typeEntry.getKey())) {
                // If we're not including this type, skip to the next.
                continue;
            }

            for (String entry : typeEntry.getValue()) {
                // Skip over duplicates
                if (result.hasResult(entry)) {
                    continue;
                }

                if (caseSensitive && entry.startsWith(partial)) {
                    result.addResult(entry);
                } else if (!caseSensitive && entry.toLowerCase(Locale.getDefault())
                        .startsWith(partial.toLowerCase(Locale.getDefault()))) {
                    result.addResult(entry);
                }
            }
        }

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
        entries.add(type, entry);

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
    public void addEntries(final TabCompletionType type, final List<String> newEntries) {
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
        entries.clear(type);
    }

    /**
     * Retrieves intelligent results for a deferred command.
     *
     * @param arg     The argument number that is being requested
     * @param context Intelligent tab completion context
     * @param offset  The number of arguments our command used before deferring to this method
     *
     * @return Additional tab targets for the text, or null if none are available
     */
    public static AdditionalTabTargets getIntelligentResults(final int arg,
            final IntelligentCommandContext context, final int offset) {
        if (arg == offset) {
            final AdditionalTabTargets targets = new AdditionalTabTargets().excludeAll();
            targets.include(TabCompletionType.COMMAND);
            return targets;
        } else {
            return getIntelligentResults(context.getWindow(),
                    new CommandArguments(context.getWindow().getCommandParser().getCommandManager(),
                            context.getPreviousArgs().subList(offset,
                                    context.getPreviousArgs().size())), context.getPartial());
        }
    }

    /**
     * Retrieves the intelligent results for the command and its arguments formed from args.
     *
     * @param window  The input window the results are required for
     * @param args    The input arguments
     * @param partial The partially-typed word being completed (if any)
     *
     * @return Additional tab targets for the text, or null if none are available
     *
     * @since 0.6.4
     */
    private static AdditionalTabTargets getIntelligentResults(
            final FrameContainer window,
            final CommandArguments args, final String partial) {
        if (!args.isCommand()) {
            return null;
        }

        final Map.Entry<CommandInfo, Command> command = window.getCommandParser().
                getCommandManager().getCommand(args.getCommandName());

        AdditionalTabTargets targets = null;

        if (command != null) {
            if (command.getValue() instanceof IntelligentCommand) {
                targets = ((IntelligentCommand) command.getValue())
                        .getSuggestions(args.getArguments().length,
                                new IntelligentCommandContext(window,
                                        Arrays.asList(args.getArguments()), partial));
            }

            if (command.getKey().getType().equals(CommandType.TYPE_CHANNEL)) {
                if (targets == null) {
                    targets = new AdditionalTabTargets();
                }

                targets.include(TabCompletionType.CHANNEL);
            }
        }

        return targets;
    }

    /**
     * Handles potentially intelligent tab completion.
     *
     * @param window  The input window the results are required for
     * @param text    The text that is being completed
     * @param partial The partially-typed word being completed (if any)
     *
     * @return Additional tab targets for the text, or null if none are available
     *
     * @since 0.6.4
     */
    public static AdditionalTabTargets getIntelligentResults(
            final FrameContainer window, final String text,
            final String partial) {
        return getIntelligentResults(window,
                new CommandArguments(window.getCommandParser().getCommandManager(), text), partial);
    }

}
