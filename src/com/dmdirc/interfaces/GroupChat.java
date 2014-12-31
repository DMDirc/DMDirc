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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.Topic;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * A chat containing multiple participants.
 */
public interface GroupChat extends Chat {

    /**
     * Returns the current topic for this channel.
     *
     * @return Current channel topic
     */
    Optional<Topic> getCurrentTopic();

    /**
     * Gets an event bus which will only contain events generated in relation to this channel.
     *
     * @return An event bus scoped to this channel.
     */
    DMDircMBassador getEventBus();

    /**
     * Retrieves the maximum length that a topic on this channel can be.
     *
     * @return The maximum length that this channel's topic may be
     */
    int getMaxTopicLength();

    /**
     * Retrieve the topics that have been seen on this channel.
     *
     * @return A list of topics that have been seen on this channel, including the current one.
     */
    List<Topic> getTopics();

    /**
     * Determines if we are currently joined to the chat.
     *
     * @return True if joined, false otherwise.
     */
    boolean isOnChannel();

    /**
     * Joins the specified channel. This only makes sense if used after a call to part().
     */
    void join();

    /**
     * Parts this channel with the specified message. Parting does NOT close the channel window.
     *
     * @param reason The reason for parting the channel
     */
    void part(final String reason);

    /**
     * Requests all available list modes for this channel.
     */
    void retrieveListModes();

    /**
     * Attempts to set the topic of this channel.
     *
     * @param topic The new topic to be used. An empty string will clear the current topic
     */
    void setTopic(final String topic);

    /**
     * Gets the name of the chat.
     *
     * @return The current name of the group chat.
     */
    String getName();

    /**
     * Tries to retrieve a {@link GroupChatUser} from the current {@link GroupChat} for the
     * specified {@link User}.
     *
     * @param user User to find
     *
     * @return User on channel, or empty if the user is not on the channel
     */
    Optional<GroupChatUser> getUser(final User user);

    /**
     * Returns the users available on this GroupChat.
     *
     * @return Users in the GroupChat
     */
    Collection<GroupChatUser> getUsers();

    /**
     * Gets the core model for the input/output window for this connection.
     *
     * @return A model for windows based on this connection.
     */
    FrameContainer getWindowModel();

    /**
     * Kicks the specified user, optionally with the specified message.
     *
     * @param user   User to kick
     * @param reason Reason for the kick
     */
    void kick(final GroupChatUser user, final Optional<String> reason);

    /**
     * Returns the available channel modes applicable to users.
     *
     * @return User modes in ascending order, or an empty string if there is no connection
     */
    String getUserModes();

    /**
     * Returns the available boolean modes.
     *
     * @return Boolean modes or an empty string if there is no connection
     */
    String getBooleanModes();

    /**
     * Returns the available list modes.
     *
     * @return List modes or an empty string if there is no connection
     */
    String getListModes();

    /**
     * Returns the available parameter modes.  Parameter modes need a parameter to set, but not to
     * unset.
     *
     * @return Parameter modes or an empty string if there is no connection
     */
    String getParameterModes();

    /**
     * Returns the available double parameter modes.  Double parameter modes need a parameter to
     * both set and unset.
     *
     * @return Double parameter modes or an empty string if there is no connection
     */
    String getDoubleParameterModes();

    /**
     * Returns the maximum number list modes of a certain type that can be set.
     *
     * @param mode Mode to query
     *
     * @return Maximum modes that can be set, or -1 if there is no connection
     */
    int getMaxListModes(final char mode);

}
