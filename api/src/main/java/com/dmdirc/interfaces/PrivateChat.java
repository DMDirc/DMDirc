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

package com.dmdirc.interfaces;

/**
 * A chat with a single remote user.
 */
public interface PrivateChat extends Chat {

    /**
     * Returns the current nickname of the user that this query is with.
     *
     * @return The nickname of this query's user
     */
    String getNickname();

    /**
     * Sends a line to the recipient of this query using the specified nickname or hostmask as a
     * target. This allows for users to send messages with a server specified (e.g.
     * <code>nick@server</code>) as though the query wasn't open.
     * <p>
     * The caller is responsible for ensuring that the <code>target</code> does actually correspond
     * to this query.
     *
     * @param line   The line to be sent
     * @param target The target to send the line to
     */
    void sendLine(final String line, final String target);

    /**
     * Returns the {@link User} associated with this query.
     *
     * @return User for this query
     */
    User getUser();

}
