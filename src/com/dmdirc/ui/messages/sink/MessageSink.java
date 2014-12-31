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

package com.dmdirc.ui.messages.sink;

import com.dmdirc.FrameContainer;

import java.util.Date;
import java.util.regex.Pattern;

/**
 * Represents a possible destination (sink) for a generic DMDirc message.
 */
public interface MessageSink {

    /**
     * Returns a regular expression pattern that can be used to determine if this sink matches a
     * given configuration entry. If the pattern contains groups, the values of the matched groups
     * are passed into the handleMessage method.
     *
     * @return Pattern to matches a config entry
     */
    Pattern getPattern();

    /**
     * Handles a message which has been directed to this sink.
     *
     * @param dispatcher     The manager that is dispatching the message
     * @param source         The original source of the message
     * @param patternMatches An array of groups matched from this sink's pattern
     * @param date           The date at which the message occurred
     * @param messageType    The type of the message (used for formatting)
     * @param args           The message arguments
     */
    void handleMessage(final MessageSinkManager dispatcher,
            final FrameContainer source,
            final String[] patternMatches, final Date date,
            final String messageType, final Object... args);

}
