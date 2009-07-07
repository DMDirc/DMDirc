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
 * A more specialised type of {@link ClientInfo} which represents a locally
 * controlled client, and as such can be modified in various ways.
 *
 * @author chris
 * @since 0.6.3m1
 */
public interface LocalClientInfo extends ClientInfo {

    /**
     * Sets this client's nickname to the specified value.
     *
     * @param name The new nickname to use
     */
    void setNickname(String name);

    /**
     * Retrieves the modes which are currently set on the local client.
     *
     * @return A string representation of the client's user modes
     */
    String getModes();

    /**
     * Alters the user modes of the local client. This method will queue modes
     * to be sent in one go, according to the behaviour/configuration of the
     * backend system. If fewer modes are altered than the queue accepts,
     * {@link #flushModes()} must be called.
     *
     * @param add Whether to add or remove the mode
     * @param mode Character representation of the mode to be altered
     */
    public void alterMode(boolean add, Character mode);

    /**
     * Flushes any modes that have been queued by the
     * {@link #alterMode(boolean, Character)} method.
     */
    public void flushModes();

}
