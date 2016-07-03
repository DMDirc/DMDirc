package com.dmdirc;

import com.dmdirc.interfaces.Connection;
import com.dmdirc.parser.interfaces.Parser;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory for {@link MessageEncoder}s.
 */
@Singleton
public class MessageEncoderFactory {

    @Inject
    public MessageEncoderFactory() {
    }

    public MessageEncoder getMessageEncoder(final Connection connection, final Parser parser) {
        return new MessageEncoder(connection, parser);
    }

}
