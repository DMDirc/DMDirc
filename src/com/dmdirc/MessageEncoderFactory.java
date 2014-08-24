package com.dmdirc;

import com.dmdirc.parser.interfaces.Parser;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory for {@link MessageEncoder}s.
 */
@Singleton
public class MessageEncoderFactory {

    private final DMDircMBassador eventBus;

    @Inject
    public MessageEncoderFactory(final DMDircMBassador eventBus) {
        this.eventBus = eventBus;
    }

    public MessageEncoder getMessageEncoder(final Server server, final Parser parser) {
        return new MessageEncoder(server, parser, eventBus);
    }

}
