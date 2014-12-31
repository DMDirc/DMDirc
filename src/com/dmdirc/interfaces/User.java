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

package com.dmdirc.interfaces;

import com.dmdirc.parser.common.AwayState;

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
     * Sets the nickname for this client.
     *
     * @param nickname New nickname for this client
     */
    void setNickname(final String nickname);

    /**
     * Retrieves the username or ident used by this client.
     *
     * @return This client's username
     */
    Optional<String> getUsername();

    /**
     * Sets the username for this client.
     *
     * @param username New username
     */
    void setUsername(final Optional<String> username);

    /**
     * Retrieves the hostname that this client is connecting from.
     *
     * @return This client's hostname
     */
    Optional<String> getHostname();

    /**
     * Sets the hostname for this client.
     *
     * @param hostname New hostname
     */
    void setHostname(final Optional<String> hostname);

    /**
     * Retrieves the full/real name of the client.
     *
     * @return This client's real name
     */
    Optional<String> getRealname();

    /**
     * Sets the realname for the client.
     *
     * @param realname New realname
     */
    void setRealname(final Optional<String> realname);

    /**
     * Retries the {@link GroupChat}s the client is a member of.
     *
     * @return Collection of {@link GroupChat}s, may be empty
     */
    Collection<GroupChat> getGroupChats();

    /**
     * Adds a {@link GroupChat} to the list of {@link GroupChat}s this user is on.
     *
     * @param groupChat GroupChat to add
     */
    void addGroupChat(final GroupChat groupChat);

    /**
     * Deletes a {@link GroupChat} from the list of {@link GroupChat}s this user is on.
     *
     * @param groupChat GroupChat to add
     */
    void delGroupChat(final GroupChat groupChat);

    /**
     * Returns the away message for the client, if the away message is empty the user is not away.
     *
     * @return Optional.empty if the user is not away, wrapped away reason if they are
     */
    Optional<String> getAwayMessage();

    /**
     * Sets the away message for the client.
     *
     * @param awayMessage Optional.empty if the user is not away, wrapped away reason if they are
     */
    void setAwayMessage(final Optional<String> awayMessage);

    /**
     * Returns the away state for this user.
     *
     * @return The user's away state
     */
    AwayState getAwayState();

    /**
     * Gets the {@link Connection} for this client.
     *
     * @return Non-Null connection for this connection
     */
    Connection getConnection();
}
