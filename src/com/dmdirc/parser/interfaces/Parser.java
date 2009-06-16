/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.parser.interfaces;

/**
 * A parser connects to a back-end chat system and handles all communication
 * with it.
 *
 * @author chris
 */
public interface Parser extends Runnable {

    /**
     * Disconnect from server. This method will quit and automatically close the
     * socket without waiting for the server.
     *
     * @param message Reason for quitting.
     */
    void disconnect(String message);

    /**
     * Join a Channel.
     *
     * @param channel Name of channel to join
     */
    void joinChannel(String channel);

    /**
     * Set the current Value of bindIP.
     *
     * @param ip New value to set bindIP
     */
    void setBindIP(String ip);

    /**
     * Determines the maximimum length a message of the specified type may be.
     *
     * @param type Type of message (eg PRIVMSG)
     * @param target Target of message (eg channel name)
     * @return The maximum length of the message
     */
    int getMaxLength(final String type, final String target);

}
