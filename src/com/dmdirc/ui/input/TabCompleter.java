/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.util.MapList;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The tab completer handles a user's request to tab complete some word.
 * 
 * @author chris
 */
public final class TabCompleter implements Serializable {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /**
     * The parent TabCompleter. Results from parents are merged with results
     * from this completer.
     */
    private TabCompleter parent;

    /**
     * The entries in this completer.
     */
    private final MapList<TabCompletionType, String> entries
            = new MapList<TabCompletionType, String>();
    
    /** Creates a new instance of TabCompleter. */
    public TabCompleter() {
        //Do nothing.
    }
    
    /**
     * Creates a new instance of TabCompleter, with a designated parent.
     * @param newParent The parent TabCompleter, which is checked for matches if
     * local ones fail
     */
    public TabCompleter(final TabCompleter newParent) {
        this.parent = newParent;
    }
    
    /**
     * Attempts to complete the partial string.
     *
     * @param partial The string to tab complete
     * @param additionals A list of additional strings to use
     * @return A TabCompleterResult containing any matches found
     */
    public TabCompleterResult complete(final String partial,
            final AdditionalTabTargets additionals) {
        final TabCompleterResult result = new TabCompleterResult();
        
        final MapList<TabCompletionType, String> targets
                = new MapList<TabCompletionType, String>(entries);
        
        final boolean caseSensitive = IdentityManager.getGlobalConfig()
                .getOptionBool("tabcompletion", "casesensitive");
        final boolean allowEmpty = IdentityManager.getGlobalConfig()
                .getOptionBool("tabcompletion", "allowempty");

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
     * @param type The type of the entry that's being added
     * @param entry The new entry to be added
     */
    public void addEntry(final TabCompletionType type, final String entry) {
        entries.add(type, entry);
    }
    
    /**
     * Adds multiple new entries to this tab completer's list.
     * 
     * @param type The type of the entries that're being added
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
     * @param type The type of the entry that should be removed
     * @param entry The entry to be removed
     */
    public void removeEntry(final TabCompletionType type, final String entry) {
        entries.remove(type, entry);
    }
    
    /**
     * Replaces the current entries with the new list.
     * 
     * @param type The type of entry which should be replaced
     * @param newEntries the new entries to use
     */
    public void replaceEntries(final TabCompletionType type, final List<String> newEntries) {
        entries.clear(type);
        entries.add(type, newEntries);
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
     * @param arg The argument number that is being requested
     * @param previousArgs The full list of previous arguments
     * @param offset The number of arguments our command used before deferring
     * to this method
     * @return Additional tab targets for the text, or null if none are available
     */
    public static AdditionalTabTargets getIntelligentResults(final int arg,
            final List<String> previousArgs, final int offset) {
        if (arg == offset) {
            final AdditionalTabTargets targets = new AdditionalTabTargets().excludeAll();
            targets.include(TabCompletionType.COMMAND);
            return targets;
        } else {
            return TabCompleter.getIntelligentResults(previousArgs.subList(offset,
                    previousArgs.size()));
        }        
    }
    
    /**
     * Retrieves the intelligent results for the command and its arguments
     * formed from args.
     * 
     * @param args A list of "words" in the input
     * @return Additional tab targets for the text, or null if none are available
     */
    private static AdditionalTabTargets getIntelligentResults(final List<String> args) {
        if (args.isEmpty() || args.get(0).charAt(0) != CommandManager.getCommandChar()) {
            return null;
        }
        
        final String signature = args.get(0).substring(1);
        final Map.Entry<CommandInfo, Command> command = CommandManager.getCommand(signature);
        
        if (command != null && command.getValue() instanceof IntelligentCommand) {
            return ((IntelligentCommand) command.getValue()).getSuggestions(args.size() - 1,
                    args.subList(1, args.size()));
        } else {
            return null;
        }        
    }
    
    /**
     * Handles potentially intelligent tab completion.
     *
     * @param text The text that is being completed
     * @return Additional tab targets for the text, or null if none are available
     */
    public static AdditionalTabTargets getIntelligentResults(final String text) {
        return getIntelligentResults(Arrays.asList(text.split(" ")));
    }
}
