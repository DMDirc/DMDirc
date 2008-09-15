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

package com.dmdirc;

import com.dmdirc.util.RollingList;

/**
 * Describes the status of a server and manages transitions between different
 * states.
 *
 * @since 0.6.3
 * @author chris
 */
public class ServerStatus {

    /** The current state of the server. */
    protected ServerState state = ServerState.DISCONNECTED;

    /** A history of transactions for debugging purposes. */
    protected RollingList<String> history = new RollingList<String>(10);

    /**
     * Transitions the status of this object to the specified state.
     *
     * @param newState The state to transition to
     */
    public synchronized void transition(final ServerState newState) {
        addHistoryEntry(state, newState);

        if (state.canTransitionTo(newState)) {
            state = newState;
        } else {
            throw new IllegalArgumentException("Illegal server state "
                    + "transition\n\n" + getTransitionHistory());
        }
    }

    /**
     * Retrieves the current state of this status object.
     *
     * @return This object's current state
     */
    public synchronized ServerState getState() {
        return state;
    }

    /**
     * Adds a history entry to this status object. The history entry contains
     * the name of the states being transitioned between, the details of the
     * method (and class and line) which initiated the transition, and the
     * name of the thread in which the transition is occuring.
     *
     * @param fromState The state which is being transitioned from
     * @param toState The state which is being transitioned to
     */
    protected void addHistoryEntry(final ServerState fromState, final ServerState toState) {
        final StringBuilder builder = new StringBuilder();
        builder.append(fromState.name());
        builder.append('→');
        builder.append(toState.name());
        builder.append(' ');
        builder.append(Thread.currentThread().getStackTrace()[2].toString());
        builder.append(" [");
        builder.append(Thread.currentThread().getName());
        builder.append(']');

        history.add(builder.toString());
    }

    /**
     * Retrieves the transition history of this status object as a string.
     *
     * @see #addHistoryEntry(com.dmdirc.ServerState, com.dmdirc.ServerState)
     * @return A line feed ('\n') delimited string containing one entry for
     * each of the entries in this status's transition history.
     */
    public String getTransitionHistory() {
        final StringBuilder builder = new StringBuilder();

        for (String line : history.getList()) {
            if (builder.length() > 0) {
                builder.append('\n');
            }

            builder.append(line);
        }

        return builder.toString();
    }

}
