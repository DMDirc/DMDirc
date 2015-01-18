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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.logger.ErrorLevel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Manages message sinks and facilitates dispatching of messages to sinks.
 */
public class MessageSinkManager {

    /** The configuration domain to use for looking up default sinks. */
    public static final String CONFIG_DOMAIN = "notifications";
    /** The default sink to use if none is specified or in case of error. */
    public static final String DEFAULT_SINK = "self";
    /** A list of known sinks. */
    private final List<MessageSink> sinks = new ArrayList<>();
    /** Event bus to post errors to. */
    private final DMDircMBassador eventBus;

    public MessageSinkManager(final DMDircMBassador eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Adds a new sink to the list of known sinks.
     *
     * @param sink The sink to be added
     */
    public void addSink(final MessageSink sink) {
        sinks.add(sink);
    }

    /**
     * Removes an existing sink from the list of known sinks.
     *
     * @param sink The sink to be removed
     */
    public void removeSink(final MessageSink sink) {
        sinks.remove(sink);
    }

    /**
     * Dispatches a message to the appropriate sink. This method will attempt to select an
     * appropriate target sink from the user's configuration.
     *
     * @param source      The source of the message
     * @param date        The date at which the message occurred
     * @param messageType The type (or 'format') of the message
     * @param args        The message arguments
     */
    public void dispatchMessage(final WindowModel source, final Date date,
            final String messageType, final Object... args) {
        final String target;
        if (source.getConfigManager().hasOptionString(CONFIG_DOMAIN, messageType)) {
            target = source.getConfigManager().getOption(CONFIG_DOMAIN, messageType);
        } else {
            target = DEFAULT_SINK;
        }

        dispatchMessage(source, date, messageType, target, args);
    }

    /**
     * Dispatches a message to the appropriate sink.
     *
     * @param source      The source of the message
     * @param date        The date at which the message occurred
     * @param messageType The type (or 'format') of the message
     * @param targetSink  The textual representation of the destination sink
     * @param args        The message arguments
     */
    public void dispatchMessage(final WindowModel source, final Date date,
            final String messageType, final String targetSink, final Object... args) {
        for (MessageSink sink : sinks) {
            final Matcher matcher = sink.getPattern().matcher(targetSink);

            if (matcher.matches()) {
                final String[] matches = new String[matcher.groupCount()];

                for (int i = 0; i < matcher.groupCount(); i++) {
                    matches[i] = matcher.group(i + 1);
                }

                sink.handleMessage(this, source, matches, date, messageType, args);
                return;
            }
        }

        // None of the sinks matched :(
        eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, null,
                "Invalid target message sink for type " + messageType + ": " + targetSink, ""));

        if (!DEFAULT_SINK.equals(targetSink)) {
            dispatchMessage(source, date, messageType, DEFAULT_SINK, args);
        }
    }

}
