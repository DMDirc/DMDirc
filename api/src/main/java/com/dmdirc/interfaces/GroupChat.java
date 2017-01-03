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

package com.dmdirc.interfaces;

import com.dmdirc.Topic;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.parser.common.ChannelListModeItem;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * A chat containing multiple participants.
 */
public interface GroupChat extends Chat {

    /**
     * Enumeration of reasons for us leaving a group chat.
     */
    enum PartReason {
        /** We disconnected from the server. */
        DISCONNECTED,
        /** We issued a part request ourselves. */
        LOCAL_PART,
        /** A part for our user came from the server. */
        REMOTE_PART
    }

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
    EventBus getEventBus();

    /**
     * Retrieves the maximum length that a topic on this channel can be.
     *
     * @return The maximum length that this channel's topic may be
     */
    int getMaxTopicLength();

    /**
     * Refreshes the list of clients stored by this channel. Should be called when (visible) user
     * modes or nicknames change.
     */
    // TODO: Should probably not need this.
    void refreshClients();

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
     * Returns the reason behind our last parting from this group chat. For example, if the
     * user manually left a chat and then subsequently disconnected from the server, the part
     * reason will be {@link PartReason#LOCAL_PART}.
     *
     * @return The reason for parting the chat. {@code null} if the chat has never been left.
     */
    PartReason getPartReason();

    /**
     * Joins the specified channel. This only makes sense if used after a call to part().
     */
    void join();

    /**
     * Parts this channel with the specified message. Parting does NOT close the channel window.
     *
     * @param reason The reason for parting the channel
     */
    void part(String reason);

    /**
     * Requests all available list modes for this channel.
     */
    void retrieveListModes();

    /**
     * Attempts to set the topic of this channel.
     *
     * @param topic The new topic to be used. An empty string will clear the current topic
     */
    void setTopic(String topic);

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
    Optional<GroupChatUser> getUser(User user);

    /**
     * Returns the users available on this GroupChat.
     *
     * @return Users in the GroupChat
     */
    Collection<GroupChatUser> getUsers();

    /**
     * Kicks the specified user, optionally with the specified message.
     *
     * @param user   User to kick
     * @param reason Reason for the kick
     */
    void kick(GroupChatUser user, Optional<String> reason);

    /**
     * Gets the items in the specified list mode category.
     *
     * @param mode The list mode to retrieve items for.
     * @return The known mode entries.
     */
    // TODO: Return a parser-neutral type
    // TODO: Don't assume mode types are always chars, use an abstraction
    Collection<ChannelListModeItem> getListModeItems(char mode);

    /**
     * Sets a mode on the group chat.
     *
     * @param mode The mode to set.
     * @param value The value of the mode, if any.
     */
    void setMode(char mode, @Nullable String value);

    /**
     * Unsets a mode on the group chat.
     *
     * @param mode The mode to unset.
     * @param value The value of the mode, if any.
     */
    void removeMode(char mode, String value);

    /**
     * Flushes all pending mode changes.
     */
    // TODO: Add a builder-type interface for batch mode changes, instead of add+flush.
    void flushModes();

    /**
     * Gets boolean modes for this chat.
     *
     * @return The current boolean (toggleable) modes.
     */
    // TODO: Don't use a String here, return a collection of some abstraction.
    String getModes();

    /**
     * Gets the value of the given mode for this chat.
     *
     * @param mode The mode to retrieve.
     * @return The current value of the mode.
     */
    String getModeValue(final char mode);

    /**
     * Requests information about all the {@link User}s in this GroupChat.
     */
    void requestUsersInfo();
}
