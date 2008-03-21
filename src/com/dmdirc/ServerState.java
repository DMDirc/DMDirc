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

/** An enumeration of possible states for servers. */
public enum ServerState {
    /** Indicates the client is in the process of connecting. */
    CONNECTING,
    /** Indicates the client has connected to the server. */
    CONNECTED,
    /** Indicates that we've been temporarily disconnected. */
    TRANSIENTLY_DISCONNECTED,
    /** Indicates that the user has told us to disconnect. */
    DISCONNECTED,
    /** In the process of disconnecting. */
    DISCONNECTING,
    /** Indicates we're waiting for the auto-reconnect timer to fire. */
    RECONNECT_WAIT,
    /** Indicates that the server frame and its children are closing. */
    CLOSING,
}