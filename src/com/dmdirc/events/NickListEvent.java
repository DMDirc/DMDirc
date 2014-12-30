package com.dmdirc.events;

import com.dmdirc.Channel;
import com.dmdirc.interfaces.GroupChat;

/**
 * Base class for all nicklist events in a {@link GroupChat}.
 */
public abstract class NickListEvent extends ChannelEvent {

    public NickListEvent(final long timestamp, final Channel channel) {
        super(timestamp, channel);
    }

    public NickListEvent(final Channel channel) {
        super(channel);
    }
}
