/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.GroupChat;

import java.util.Collection;
import java.util.Optional;

/**
 * Holds information about a {@link User} on a {@link Connection}
 */
public interface User {
    /**
     * Retrieves the nickname or display name used by this client.
     *
     * @return This client's nickname
     */
    String getNickname();

    /**
     * Retrieves the username or ident used by this client.
     *
     * @return This client's username
     */
    String getUsername();

    /**
     * Retrieves the hostname that this client is connecting from.
     *
     * @return This client's hostname
     */
    String getHostname();

    /**
     * Retrieves the full/real name of the client.
     *
     * @return This client's real name
     */
    String getRealname();

    Collection<GroupChat> getGroupChats();

    Optional<String> getAwayMessage();
}
