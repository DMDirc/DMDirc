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

import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.interfaces.Chat;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.messages.sink.MessageSinkManager;
import com.dmdirc.util.URLBuilder;

import java.util.Collection;

import javax.annotation.Nullable;

/**
 * Defines common methods for objects that you can send messages to (such as channels and queries).
 */
public abstract class MessageTarget extends FrameContainer implements Chat {

    /**
     * Creates a new MessageTarget.
     */
    public MessageTarget(
            @Nullable final FrameContainer parent,
            final String icon,
            final String name,
            final String title,
            final AggregateConfigProvider config,
            final BackBufferFactory backBufferFactory,
            final CommandParser parser,
            final TabCompleter tabCompleter,
            final MessageSinkManager messageSinkManager,
            final URLBuilder urlBuilder,
            final DMDircMBassador eventBus,
            final Collection<String> components) {
        super(parent, icon, name, title, config, backBufferFactory, urlBuilder, parser,
                tabCompleter, messageSinkManager, eventBus, components);
    }

    /**
     * Sends the specified string as an action (CTCP) to the target that this object represents.
     *
     * @param action The action to send
     */
    public abstract void sendAction(final String action);

}
