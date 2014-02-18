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

package com.dmdirc.messages;

import com.dmdirc.Channel;
import com.dmdirc.WritableFrameContainer;

import java.util.Date;
import java.util.regex.Pattern;

/**
 * A message sink which adds the message to all channels the local user has in common with a
 * specified remote user. An optional fallback may be specified for use if there are no common
 * channels with the user. If no fallback is specified and there are no common channels, the message
 * is despatched to the source.
 */
public class CommonChanelsMessageSink implements MessageSink {

    /** The pattern to use to match this sink. */
    private static final Pattern PATTERN = Pattern.compile("comchans:(.*?)(?:\\s(.*))?");

    /** {@inheritDoc} */
    @Override
    public Pattern getPattern() {
        return PATTERN;
    }

    /** {@inheritDoc} */
    @Override
    public void handleMessage(final MessageSinkManager despatcher,
            final WritableFrameContainer source,
            final String[] patternMatches, final Date date,
            final String messageType, final Object... args) {
        final String user = String.format(patternMatches[0], args);
        boolean found = false;

        for (String channelName : source.getConnection().getChannels()) {
            final Channel channel = source.getConnection().getChannel(channelName);
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
                despatcher.despatchMessage(source, date, messageType,
                        patternMatches[1], args);
            }
        }
    }

}
