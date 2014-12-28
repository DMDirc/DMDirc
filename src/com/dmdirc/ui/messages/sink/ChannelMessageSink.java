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

package com.dmdirc.ui.messages.sink;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.interfaces.Connection;

import java.util.Date;
import java.util.regex.Pattern;

import javax.inject.Inject;

/**
 * A message sink which adds the message to a named channel. An optional fallback may be specified
 * for use if the channel does not exist. If no fallback is specified and the channel does not
 * exist, the message is dispatched to the source.
 */
public class ChannelMessageSink implements MessageSink {

    /** The pattern to use to match this sink. */
    private static final Pattern PATTERN = Pattern.compile("channel:(.*?)(?:\\s(.*))?");

    @Inject
    public ChannelMessageSink() {
    }

    @Override
    public Pattern getPattern() {
        return PATTERN;
    }

    @Override
    public void handleMessage(final MessageSinkManager dispatcher,
            final FrameContainer source,
            final String[] patternMatches, final Date date,
            final String messageType, final Object... args) {
        final String user = String.format(patternMatches[0], args);
        boolean found = false;

        final Connection connection = source.getConnection().get();
        for (Channel channel : connection.getChannels()) {
            if (channel.getChannelInfo().getChannelClient(user) != null) {
                channel.addLine(messageType, date, args);
                found = true;
            }
        }

        if (!found) {
            if (patternMatches[1] == null) {
                // No fallback specified
                source.addLine(messageType, date, args);
            } else {
                // They specified a fallback
                dispatcher.dispatchMessage(source, date, messageType, patternMatches[1], args);
            }
        }
    }

}
