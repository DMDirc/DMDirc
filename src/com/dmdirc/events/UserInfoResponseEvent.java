/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.events;

import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.User;
import com.dmdirc.parser.events.UserInfoEvent;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Event raised when detailed user info has been received for a user.
 */
public class UserInfoResponseEvent extends ServerEvent {

    private final User user;
    private final Map<UserInfoEvent.UserInfoType, String> info;

    public UserInfoResponseEvent(final Connection connection, final long date,
            final User user, final Map<UserInfoEvent.UserInfoType, String> info) {
        super(date, connection);
        this.user = user;
        this.info = new EnumMap<>(info);
    }

    /**
     * Gets the client that the event is for.
     *
     * @return The user this event is for.
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets a specific piece of information about the user.
     *
     * @param type The type of information to return.
     *
     * @return An optional containing the information, if it was provided.
     */
    public Optional<String> getInfo(final UserInfoEvent.UserInfoType type) {
        return Optional.ofNullable(info.get(type));
    }
}
