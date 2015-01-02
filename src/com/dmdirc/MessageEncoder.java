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

package com.dmdirc;

import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.parser.interfaces.Encoder;
import com.dmdirc.parser.interfaces.Parser;

import java.io.UnsupportedEncodingException;

/**
 * An {@link Encoder} implementation that reads the desired encoding from the relevant target's
 * config file.
 */
public class MessageEncoder implements Encoder {

    /** The connection that owns this encoder. */
    private final Connection connection;
    /** The parser that this encoder will work for. */
    private final Parser parser;
    /** The event bus to post errors to. */
    private final DMDircMBassador eventBus;

    /**
     * Creates a new instance of {@link MessageEncoder}.
     *
     * @param connection   The connection that owns this encoder
     * @param parser   The parser that this encoder will work for
     * @param eventBus The event bus to post errors to.
     */
    public MessageEncoder(final Connection connection, final Parser parser,
            final DMDircMBassador eventBus) {
        this.connection = connection;
        this.parser = parser;
        this.eventBus = eventBus;
    }

    @Override
    public String encode(final String source, final String target,
            final byte[] message, final int offset, final int length) {
        String encoding = connection.getWindowModel().getConfigManager()
                .getOption("general", "encoding");

        if (target != null && parser.isValidChannelName(target)) {
            encoding = connection.getGroupChatManager().getChannel(target)
                    .map(GroupChat::getWindowModel)
                    .map(FrameContainer::getConfigManager)
                    .map(cm -> cm.getOption("general", "encoding"))
                    .orElse(encoding);
        }

        try {
            return new String(message, offset, length, encoding);
        } catch (UnsupportedEncodingException ex) {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, ex,
                    "Unsupported character encoding: " + encoding, ""));
            return new String(message, offset, length);
        }
    }

}
