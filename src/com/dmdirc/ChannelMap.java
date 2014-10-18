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

import com.dmdirc.parser.common.ChannelJoinRequest;
import com.dmdirc.parser.common.DefaultStringConverter;
import com.dmdirc.parser.interfaces.StringConverter;

import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility class that maintains a map of channel names to channels.
 */
public class ChannelMap {

    /** Map of lowercased channel names to channel objects. */
    private final Map<String, Channel> channels = new ConcurrentSkipListMap<>();
    /** String converter to use to lowercase names. */
    private StringConverter converter = new DefaultStringConverter();

    /**
     * Sets the string converter that will be used for any future case conversions. If the map is
     * non-empty and the converter is changed, duplicate entries may be created.
     *
     * @param converter The new converter to use.
     */
    public void setStringConverter(final StringConverter converter) {
        this.converter = checkNotNull(converter);
    }

    /**
     * Determines if the specified channel is contained in this map.
     *
     * @param channel The channel to check.
     *
     * @return True if the channel is present, false otherwise.
     */
    public boolean contains(final String channel) {
        return channels.containsKey(converter.toLowerCase(channel));
    }

    /**
     * Gets the specified channel, if it exists.
     *
     * @param channel The channel to retrieve.
     *
     * @return An optional channel.
     */
    public Optional<Channel> get(final String channel) {
        return Optional.fromNullable(channels.get(converter.toLowerCase(channel)));
    }

    /**
     * Gets a list of names of all the channels in the map.
     *
     * @return A list of lowercased names of channels in this map.
     */
    public List<String> getNames() {
        return new ArrayList<>(channels.keySet());
    }

    /**
     * Adds a channel to the map. If a channel with the same name (when lowercased) is present, it
     * will be replaced.
     *
     * @param channel The new channel to be added.
     */
    public void add(final Channel channel) {
        channels.put(converter.toLowerCase(channel.getName()), channel);
    }

    /**
     * Removes a channel from the map.
     *
     * @param channel The channel to be removed.
     */
    public void remove(final String channel) {
        channels.remove(converter.toLowerCase(channel));
    }

    /**
     * Closes all channels in the map.
     */
    public void closeAll() {
        for (Channel channel : channels.values()) {
            channel.close();
        }
    }

    /**
     * Resets the state of all channels in the map.
     */
    public void resetAll() {
        for (Channel channel : channels.values()) {
            channel.resetWindow();
        }
    }

    /**
     * Adds a line to all channels in the map.
     *
     * @param messageType The type of message to be added.
     * @param date        The time of origin of the message.
     * @param args        The arguments for the message.
     */
    public void addLineToAll(final String messageType, final Date date, final Object... args) {
        for (Channel channel : channels.values()) {
            channel.addLine(messageType, date, args);
        }
    }

    /**
     * Gets a representation of the channels in this map as a collection of join requests.
     *
     * @return A collection of join requests corresponding to channels in this map.
     */
    public Collection<ChannelJoinRequest> asJoinRequests() {
        return channels.values().stream()
                .map(channel -> new ChannelJoinRequest(channel.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Creates a runnable that, when run, will cause each channel in the map at that time to check
     * its 'who' status.
     *
     * @return A new runnable.
     */
    public Runnable getWhoRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                for (Channel channel : channels.values()) {
                    channel.checkWho();
                }
            }
        };
    }

}
