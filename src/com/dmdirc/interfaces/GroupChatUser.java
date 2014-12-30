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

import com.dmdirc.events.DisplayProperty;
import com.dmdirc.events.DisplayPropertyMap;

import java.util.Optional;

/**
 * Describes a {@link User} that is present on a {@link GroupChat}.
 */
public interface GroupChatUser {

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
     * Returns the most important mode that the client holds in its textual
     * form (e.g. o, v, etc)
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
     * Sets a property relating to how this {@link GroupChatUser} should be displayed.
     *
     * @param property The property to be set
     * @param value The value of the property
     * @param <T> The type of value that the property takes.
     */
    <T> void setDisplayProperty(final DisplayProperty<T> property, final T value);

    /**
     * Retrieves a property relating to how this {@link GroupChatUser} should be displayed.
     *
     * @param property The property to be retrieved.
     * @param <T> The type of value that the property takes.
     * @return An optional value for the property.
     */
    <T> Optional<T> getDisplayProperty(final DisplayProperty<T> property);

    /**
     * Removes a property relating to how this {@link GroupChatUser} should be displayed.
     */
    <T> void removeDisplayProperty(final DisplayProperty<T> property);

    /**
     * Gets the map of all display properties for this {@link GroupChatUser}.
     *
     * @return The map of display properties.
     */
    DisplayPropertyMap getDisplayProperties();
}
