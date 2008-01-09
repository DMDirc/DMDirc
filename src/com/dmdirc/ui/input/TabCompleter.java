/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.config.IdentityManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The tab completer handles a user's request to tab complete some word.
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
    private List<String> entries = new ArrayList<String>();
    
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
        
        final List<String> targets = new ArrayList<String>();
        
        final boolean caseSensitive = IdentityManager.getGlobalConfig()
                .getOptionBool("tabcompletion", "casesensitive", false);
                
        if (additionals != null) {
            targets.addAll(additionals);
        }
        
        if (additionals == null || additionals.shouldIncludeNormal()) {
            targets.addAll(entries);
        }
        
        for (String entry : targets) {
            // Skip over duplicates
            if (result.hasResult(entry)) {
                continue;
            }
            
            if (caseSensitive) {
                if (entry.startsWith(partial)) {
                    result.addResult(entry);
                }
            } else {
                if (entry.toLowerCase(Locale.getDefault())
                        .startsWith(partial.toLowerCase(Locale.getDefault()))) {
                    result.addResult(entry);
                }
            }
        }
        
        if (parent != null && (additionals == null || additionals.shouldIncludeNormal())) {
            result.merge(parent.complete(partial, null));
        }
        return result;
    }
    
    /**
     * Adds a new entry to this tab completer's list.
     * @param entry The new entry to be added
     */
    public void addEntry(final String entry) {
        entries.add(entry);
    }
    
    /**
     * Adds multiple new entries to this tab completer's list.
     * @param newEntries Entries to be added
     */
    public void addEntries(final List<String> newEntries) {
        if (newEntries == null) {
            return;
        }
        
        for (String entry : newEntries) {
            addEntry(entry);
        }
    }
    
    /**
     * Removes a specified entry from this tab completer's list.
     * @param entry The entry to be removed
     */
    public void removeEntry(final String entry) {
        entries.remove(entry);
    }
    
    /**
     * Replaces the current entries with the new list.
     * @param newEntries the new entries to use
     */
    public void replaceEntries(final List<String> newEntries) {
        entries = newEntries;
    }
    
    /**
     * Clears all entries in this tab completer.
     */
    public void clear() {
        entries.clear();
    }
    
}
