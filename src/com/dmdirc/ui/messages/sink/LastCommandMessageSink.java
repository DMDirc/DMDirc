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

import com.dmdirc.FrameContainer;
import com.dmdirc.Server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

/**
 * A message sink which adds the message to the window where a command matching the provided regex
 * was most recently executed. If no such window is found, falls back to the source.
 */
public class LastCommandMessageSink implements MessageSink {

    /** The pattern to use to match this sink. */
    private static final Pattern PATTERN = Pattern.compile("lastcommand:(.*)");

    @Inject
    public LastCommandMessageSink() {
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
        final Object[] escapedargs = new Object[args.length];

        for (int i = 0; i < args.length; i++) {
            escapedargs[i] = "\\Q" + args[i] + "\\E";
        }

        final String command = String.format(patternMatches[0], escapedargs);

        FrameContainer best = source;
        long besttime = 0;

        final List<FrameContainer> containers = new ArrayList<>();

        containers.add((Server) source.getConnection());
        containers.addAll(((Server) source.getConnection()).getChildren());

        for (FrameContainer container : containers) {
            if (!container.isWritable()) {
                continue;
            }

            final long time = container.getCommandParser().getCommandTime(command);
            if (time > besttime) {
                besttime = time;
                best = container;
            }
        }

        best.addLine(messageType, date, args);
    }

}
