/*
 * Copyright (c) 2006-2012 DMDirc Developers
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

import com.dmdirc.FrameContainer;
import com.dmdirc.WritableFrameContainer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A message sink which adds the message to the window where a command matching
 * the provided regex was most recently executed. If no such window is found,
 * falls back to the source.
 */
public class LastCommandMessageSink implements MessageSink {

    /** The pattern to use to match this sink. */
    private static final Pattern PATTERN = Pattern.compile("lastcommand:(.*)");

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
        final Object[] escapedargs = new Object[args.length];

        for (int i = 0; i < args.length; i++) {
            escapedargs[i] = "\\Q" + args[i] + "\\E";
        }

        final String command = String.format(patternMatches[0], escapedargs);

        WritableFrameContainer best = source;
        long besttime = 0;

        final List<FrameContainer> containers = new ArrayList<FrameContainer>();

        containers.add(source.getServer());
        containers.addAll(source.getServer().getChildren());

        for (FrameContainer container : containers) {
            if (!(container instanceof WritableFrameContainer)) {
                continue;
            }

            final long time = ((WritableFrameContainer) container)
                    .getCommandParser().getCommandTime(command);
            if (time > besttime) {
                besttime = time;
                best = (WritableFrameContainer) container;
            }
        }

        best.addLine(messageType, date, args);
    }

}
