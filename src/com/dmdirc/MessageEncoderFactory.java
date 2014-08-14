package com.dmdirc;

import com.dmdirc.parser.interfaces.Parser;

import com.google.common.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory for {@link MessageEncoder}s.
 */
@Singleton
public class MessageEncoderFactory {

    private final EventBus eventBus;

    @Inject
    public MessageEncoderFactory(final EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public MessageEncoder getMessageEncoder(final Server server, final Parser parser) {
        return new MessageEncoder(server, parser, eventBus);
    }

}
