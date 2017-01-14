/*
 * Copyright (c) 2006-2017 DMDirc Developers
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

import java.util.Arrays;
import java.util.List;

/**
 * An enumeration of possible states for servers.
 */
public enum ServerState {

    /** Indicates the client is in the process of connecting. */
    CONNECTING(
            "CONNECTED", // Connection attempt succeeded
            "TRANSIENTLY_DISCONNECTED", // Connection attempt failed
            "DISCONNECTING", // User ordered a disconnection
            "CLOSING" // DMDirc is closing
    ),
    /** Indicates the client has connected to the server. */
    CONNECTED(
            "DISCONNECTING", // User ordered a disconnection
            "TRANSIENTLY_DISCONNECTED", // Server caused a disconnection
            "CLOSING" // DMDirc is closing
    ),
    /** Indicates that we've been temporarily disconnected. */
    TRANSIENTLY_DISCONNECTED(
            "CONNECTING", // User forced a connect attempt
            "RECONNECT_WAIT", // Waiting for auto-reconnect
            "CLOSING" // DMDirc is closing
    ),
    /** Indicates that the user has told us to disconnect. */
    DISCONNECTED(
            "CONNECTING", // User forced a connect attempt
            "CLOSING" // DMDirc is closing
    ),
    /** In the process of disconnecting. */
    DISCONNECTING(
            "DISCONNECTED", // Socket closed
            "CLOSING" // DMDirc is closing
    ),
    /** Indicates we're waiting for the auto-reconnect timer to fire. */
    RECONNECT_WAIT(
            "CONNECTING", // User forced a connect attempt
            "TRANSIENTLY_DISCONNECTED", // Reconnect timer expired
            "DISCONNECTED", // User forced a disconnect
            "CLOSING" // DMDirc is closing
    ),
    /** Indicates that the server frame and its children are closing. */
    CLOSING;
    /** The allowed transitions from this state. */
    private final List<String> transitions;

    /**
     * Creates a new instance of ServerState.
     *
     * @since 0.6.3m1
     * @param transitions The names of the states to which a transition is allowed from this state
     */
    ServerState(final String... transitions) {
        this.transitions = Arrays.asList(transitions);
    }

    /**
     * Determines whether a transition from this state to the specified state would be legal.
     *
     * @since 0.6.3m1
     * @param state The state that is being transitioned to
     *
     * @return True if the transition is allowed, false otherwise.
     */
    public boolean canTransitionTo(final ServerState state) {
        return transitions.contains(state.name());
    }

    /**
     * Determines where the current state is a disconnected one.
     *
     * @return True if the state is one of the disconnected states, false otherwise
     *
     * @since 0.6.3m1
     */
    public boolean isDisconnected() {
        return this == ServerState.DISCONNECTED || this == ServerState.TRANSIENTLY_DISCONNECTED;
    }

}
