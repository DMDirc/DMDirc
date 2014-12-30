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
import com.dmdirc.Topic;

import java.util.List;
import java.util.Optional;

/**
 * A chat containing multiple participants.
 */
public interface GroupChat extends Chat {

    /**
     * Adds a nicklist listener to this channel.
     *
     * @param listener The listener to notify about nicklist changes.
     */
    void addNicklistListener(final NicklistListener listener);

    /**
     * Adds a topic change listener to this channel.
     *
     * @param listener The listener to notify about topic changes.
     */
    void addTopicChangeListener(final TopicChangeListener listener);

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
     * Removes a nicklist listener from this channel.
     *
     * @param listener The listener to be removed.
     */
    void removeNicklistListener(final NicklistListener listener);

    /**
     * Removes a topic change listener from this channel.
     *
     * @param listener The listener to be removed.
     */
    void removeTopicChangeListener(final TopicChangeListener listener);

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

    GroupChatUser getUser(final User user);

}
