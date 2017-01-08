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

package com.dmdirc.logger;

/**
 * Error report status.
 */
public enum ErrorReportStatus {

    /** Not applicable. */
    NOT_APPLICABLE("Not applicable", true),
    /** Finished state. */
    FINISHED("Finished", true),
    /** Sending state. */
    SENDING("Sending...", false),
    /** Error sending. */
    ERROR("Error sending", true),
    /** Report queued. */
    QUEUED("Queued", false),
    /** Waiting state. */
    WAITING("Waiting", true);
    /** toString value of the item. */
    private final String value;
    /** Whether this state is terminal. */
    private final boolean terminal;

    /**
     * Instantiates the enum.
     *
     * @param value    toString value
     * @param terminal Whether or not the state is terminal (i.e., whether there are pending actions
     *                 to be performed on the error)
     */
    ErrorReportStatus(final String value, final boolean terminal) {
        this.value = value;
        this.terminal = terminal;
    }

    /**
     * Determines whether or not this state is terminal. Terminal states are defined as those on
     * which no further actions will be performed without user interaction. Non-terminal states may
     * start or finish sending in the future.
     *
     * @return True if the state is terminal, false otherwise
     */
    public boolean isTerminal() {
        return terminal;
    }

    @Override
    public String toString() {
        return value;
    }

}
