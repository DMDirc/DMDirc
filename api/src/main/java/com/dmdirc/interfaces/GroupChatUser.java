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

import java.util.Comparator;
import java.util.Optional;

/**
 * Describes a {@link User} that is present on a {@link GroupChat}.
 */
public interface GroupChatUser extends Displayable {

    /**
     * Retrieves the {@link User} object which this object corresponds
     * to.
     *
     * @return The User object which this object represents
     */
    User getUser();

    /**
     * Retrieves the {@link GroupChat} this {@link User} is on.
     *
     * @return The corresponding GroupChat object
     */
    GroupChat getGroupChat();

    /**
     * Returns the most important mode that the client holds in its prefix
     * form (e.g. @, +, etc)
     *
     * @return The most important mode the client holds, or an empty string
     */
    String getImportantMode();

    /**
     * Returns a list of all modes known to be held by the client, in their
     * textual form (e.g. o, v, etc)
     *
     * @return All modes the client holds, or an empty string
     */
    String getAllModes();

    /**
     * Retrieves the nickname or display name used by this client.
     *
     * @return This client's nickname
     */
    String getNickname();

    /**
     * Retrieves the nickname or display name used by this client, with their most important mode
     * prefixed.
     *
     * @return This client's nickname with their important mode prefixed (if any).
     */
    String getModePrefixedNickname();

    /**
     * Retrieves the username or ident used by this client.
     *
     * @return This client's username
     */
    Optional<String> getUsername();

    /**
     * Retrieves the hostname that this client is connecting from.
     *
     * @return This client's hostname
     */
    Optional<String> getHostname();

    /**
     * Retrieves the full/real name of the client.
     *
     * @return This client's real name
     */
    Optional<String> getRealname();

    /**
     * Retrieves a comparator that can compare the important modes of a client.
     *
     * @return Mode comparator
     */
    Comparator<String> getModeComparator();

}
