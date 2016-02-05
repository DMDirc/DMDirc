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

package com.dmdirc.ui.messages.sink;

import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.User;
import com.dmdirc.interfaces.WindowModel;

import java.util.Date;
import java.util.regex.Pattern;

import javax.inject.Inject;

/**
 * A message sink which adds the message to all channels the local user has in common with a
 * specified remote user. An optional fallback may be specified for use if there are no common
 * channels with the user. If no fallback is specified and there are no common channels, the message
 * is dispatched to the source.
 */
@Deprecated
public class CommonChannelsMessageSink implements MessageSink {

    /** The pattern to use to match this sink. */
    private static final Pattern PATTERN = Pattern.compile("comchans:(.*?)(?:\\s(.*))?");

    @Inject
    public CommonChannelsMessageSink() {
    }

    @Override
    public Pattern getPattern() {
        return PATTERN;
    }

    @Override
    public void handleMessage(final MessageSinkManager dispatcher,
            final WindowModel source,
            final String[] patternMatches, final Date date,
            final String messageType, final Object... args) {
        final String username = String.format(patternMatches[0], args);
        final Connection connection = source.getConnection().get();
        final User user = connection.getUser(username);
        boolean found = false;

        for (GroupChat channel : connection.getGroupChatManager().getChannels()) {
            if (channel.getUser(user).isPresent()) {
                channel.getWindowModel().addLine(messageType, date, args);
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
