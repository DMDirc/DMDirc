package com.dmdirc.events;

import com.dmdirc.interfaces.GroupChat;

/**
 * Base class for all nicklist events in a {@link GroupChat}.
 */
public abstract class NickListEvent extends ChannelEvent {

    public NickListEvent(final long timestamp, final GroupChat channel) {
        super(timestamp, channel);
    }

    public NickListEvent(final GroupChat channel) {
        super(channel);
    }
}
