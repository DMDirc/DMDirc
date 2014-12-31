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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Describes a set of matches from a tab completion attempt.
 */
public class TabCompletionMatches {

    /**
     * The result list for this tab completer.
     */
    private final List<String> results = new ArrayList<>();

    /**
     * Adds a result to this result set.
     *
     * @param result The result to be added
     */
    public void addResult(final String result) {
        results.add(result);
    }

    /**
     * Determines if this result set contains the specified result.
     *
     * @param result The result to be tested
     *
     * @return True if this set contains the specified result, false otherwise
     */
    public boolean hasResult(final String result) {
        return results.contains(result);
    }

    /**
     * Merges the specified additional results with this result set.
     *
     * @param additional The results to merge
     */
    public void merge(final TabCompletionMatches additional) {
        additional.getResults().stream().filter(result -> !hasResult(result))
                .forEach(this::addResult);
    }

    /**
     * Gets the total size of this result set.
     *
     * @return the size of this result set
     */
    public int getResultCount() {
        return results.size();
    }

    /**
     * Retrieves the list of results that this set contains.
     *
     * @return An unmodifiable list containing the results
     */
    public List<String> getResults() {
        return Collections.unmodifiableList(results);
    }

    @Override
    public String toString() {
        final StringBuilder buff = new StringBuilder();

        for (String entry : results) {
            if (buff.length() > 0) {
                buff.append(", ");
            }

            buff.append(entry);
        }

        return buff.toString();
    }

}
