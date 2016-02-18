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

package com.dmdirc.interfaces;

import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.ui.input.TabCompleter;

/**
 * Models the input functionality of a {@link WindowModel}.
 */
public interface InputModel {

    /**
     * Sends a line of text to the entity associated with the window.
     *
     * @param line The line to be sent
     */
    void sendLine(String line);

    /**
     * Retrieves the command parser to be used for input.
     *
     * @return This model's command parser
     */
    CommandParser getCommandParser();

    /**
     * Retrieves the tab completer which should be used for input.
     *
     * @return This model's tab completer
     */
    TabCompleter getTabCompleter();

    /**
     * Returns the maximum length that a line passed to {@link #sendLine(String)} should be, in
     * order to prevent it being truncated or causing protocol violations.
     *
     * @return The maximum line length for this model
     */
    int getMaxLineLength();

    /**
     * Returns the number of lines that the specified string would be sent as, if it were passed
     * to {@link #sendLine(String)}.
     *
     * @param line The string to be split and sent
     * @return The number of lines required to send the specified string
     */
    int getNumLines(String line);

}
