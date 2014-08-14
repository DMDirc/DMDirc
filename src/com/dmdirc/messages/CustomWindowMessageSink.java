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

import com.dmdirc.CustomWindow;
import com.dmdirc.FrameContainer;
import com.dmdirc.Server;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.util.URLBuilder;

import java.util.Date;
import java.util.regex.Pattern;

import javax.inject.Inject;

/**
 * A message sink which adds the message to a custom window, creating it if necessary.
 */
public class CustomWindowMessageSink implements MessageSink {

    /** The pattern to use to match this sink. */
    private static final Pattern PATTERN = Pattern.compile("window:(.*)");
    /** Window management. */
    private final WindowManager windowManager;
    /** The URL builder to use when finding icons. */
    private final URLBuilder urlBuilder;

    /**
     * Creates a new custom window message sink.
     *
     * @param windowManager Window management
     * @param urlBuilder    The URL builder to use when finding icons.
     */
    @Inject
    public CustomWindowMessageSink(
            final WindowManager windowManager,
            final URLBuilder urlBuilder) {
        this.windowManager = windowManager;
        this.urlBuilder = urlBuilder;
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
        FrameContainer targetWindow = windowManager
                .findCustomWindow((Server) source.getConnection(), patternMatches[0]);

        if (targetWindow == null) {
            targetWindow = new CustomWindow(patternMatches[0], patternMatches[0],
                    (Server) source.getConnection(), urlBuilder);
            windowManager.addWindow((Server) source.getConnection(), targetWindow);
        }

        targetWindow.addLine(messageType, date, args);
    }

}
