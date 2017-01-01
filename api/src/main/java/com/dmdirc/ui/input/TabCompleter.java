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

import javax.annotation.Nullable;
import java.util.List;

/**
 * The tab completer handles a user's request to tab complete some word.
 */
public interface TabCompleter {

    /**
     * Attempts to complete the partial string.
     *
     * @param partial     The string to tab complete
     * @param additionals A list of additional strings to use
     *
     * @return A TabCompleterResult containing any matches found
     */
    List<String> complete(String partial, @Nullable AdditionalTabTargets additionals);

    /**
     * Adds a new entry to this tab completer's list.
     *
     * @param type  The type of the entry that's being added
     * @param entry The new entry to be added
     */
    void addEntry(TabCompletionType type, String entry);

    /**
     * Adds multiple new entries to this tab completer's list.
     *
     * @param type       The type of the entries that're being added
     * @param newEntries Entries to be added
     */
    void addEntries(TabCompletionType type, Iterable<String> newEntries);

    /**
     * Removes a specified entry from this tab completer's list.
     *
     * @param type  The type of the entry that should be removed
     * @param entry The entry to be removed
     */
    void removeEntry(TabCompletionType type, String entry);

    /**
     * Clears all entries in this tab completer.
     */
    void clear();

    /**
     * Clears all entries of the specified type in this tab completer.
     *
     * @param type The type of entry to clear
     */
    void clear(TabCompletionType type);

}
