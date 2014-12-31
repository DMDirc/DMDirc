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

package com.dmdirc.interfaces.ui;

/**
 * Search bar interface.
 */
public interface SearchBar {

    /** Direction used for searching. */
    enum Direction {

        /** Move up through the document. */
        UP,
        /** Move down through the document. */
        DOWN,

    }

    /**
     * Opens the search bar.
     */
    void open();

    /**
     * Closes the search bar.
     */
    void close();

    /**
     * Returns the current search phrase for the search bar.
     *
     * @return Current search phrase
     */
    String getSearchPhrase();

    /**
     * Returns whether the search needs to be case sensitive.
     *
     * @return true if the search needs to be case sensitive
     */
    boolean isCaseSensitive();

    /**
     * Searches the textpane for text.
     *
     * @param text          the text to search for
     * @param caseSensitive whether the search is case sensitive
     */
    void search(final String text, final boolean caseSensitive);

    /**
     * Searches the textpane for text.
     *
     * @param direction     the direction to search from
     * @param text          the text to search for
     * @param caseSensitive whether the search is case sensitive
     */
    void search(final Direction direction, final String text,
            final boolean caseSensitive);

}
