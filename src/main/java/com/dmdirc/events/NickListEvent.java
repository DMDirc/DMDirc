package com.dmdirc.events;

import com.dmdirc.interfaces.GroupChat;

import java.time.LocalDateTime;

/**
 * Base class for all nicklist events in a {@link GroupChat}.
 */
public abstract class NickListEvent extends ChannelEvent {

    public NickListEvent(final LocalDateTime timestamp, final GroupChat channel) {
        super(timestamp, channel);
    }

    public NickListEvent(final GroupChat channel) {
        super(channel);
    }
}
