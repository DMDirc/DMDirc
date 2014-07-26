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

import com.dmdirc.FrameContainer;
import com.dmdirc.ui.StatusMessage;
import com.dmdirc.ui.core.components.StatusBarManager;
import com.dmdirc.ui.messages.Formatter;

import java.util.Date;
import java.util.regex.Pattern;

import javax.inject.Inject;

/**
 * A message sink which adds the message to the status bar.
 */
public class StatusBarMessageSink implements MessageSink {

    /** The pattern to use to match this sink. */
    private static final Pattern PATTERN = Pattern.compile("statusbar");
    /** The status bar manager to add messages to. */
    private final StatusBarManager statusBarManager;

    /**
     * Creates a new instance of {@link StatusBarMessageSink}.
     *
     * @param statusBarManager The status bar manager to add messages to.
     */
    @Inject
    public StatusBarMessageSink(final StatusBarManager statusBarManager) {
        this.statusBarManager = statusBarManager;
    }

    /** {@inheritDoc} */
    @Override
    public Pattern getPattern() {
        return PATTERN;
    }

    /** {@inheritDoc} */
    @Override
    public void handleMessage(final MessageSinkManager despatcher,
            final FrameContainer source,
            final String[] patternMatches, final Date date,
            final String messageType, final Object... args) {
        final String message = Formatter.formatMessage(source.getConfigManager(),
                messageType, args);
        statusBarManager.setMessage(new StatusMessage(message, source.getConfigManager()));
    }

}
