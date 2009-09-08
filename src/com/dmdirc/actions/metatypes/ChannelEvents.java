/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.actions.metatypes;

import com.dmdirc.Channel;
import com.dmdirc.Topic;
import com.dmdirc.actions.interfaces.ActionMetaType;
import com.dmdirc.parser.interfaces.ChannelClientInfo;

/**
 * Defines channel-related events.
 *
 * @author Chris
 */
public enum ChannelEvents implements ActionMetaType {

    /** Channel event type. */
    CHANNEL_EVENT(new String[]{"channel"}, Channel.class),
    /** Channel mode event. */
    CHANNEL_MODEEVENT(new String[]{"channel", "modes"}, Channel.class, String.class),
    /** Channel topic event type. */
    CHANNEL_TOPICEVENT(new String[]{"channel", "topic"}, Channel.class, Topic.class),
    /** Channel CTCP type. */
    CHANNEL_CTCP(new String[]{"channel", "user", "type", "content"}, Channel.class, ChannelClientInfo.class, String.class, String.class),
    /** Channel event with source. */
    CHANNEL_SOURCED_EVENT(new String[]{"channel", "user"}, Channel.class, ChannelClientInfo.class),
    /** Chanel event with source and argument. */
    CHANNEL_SOURCED_EVENT_WITH_ARG(new String[]{"channel", "user", "message"}, Channel.class, ChannelClientInfo.class, String.class),
    /** Chanel event with source and nickname. */
    CHANNEL_NICKEVENT(new String[]{"channel", "user", "old nickname"}, Channel.class, ChannelClientInfo.class, String.class),
    /** Chanel event with source, message and mode character argument. @since 0.6.3m2 */
    CHANNEL_SOURCED_EVENT_WITH_CHARARG(new String[]{"channel", "user", "mode", "message"}, Channel.class, ChannelClientInfo.class, String.class, String.class),
    /** Channel event with source and victim. */
    CHANNEL_SOURCED_EVENT_WITH_VICTIM(new String[]{"channel", "user", "victim", "message"}, Channel.class, ChannelClientInfo.class, ChannelClientInfo.class, String.class);

    /** The names of the arguments for this meta type. */
    private String[] argNames;
    /** The classes of the arguments for this meta type. */
    private Class[] argTypes;

    /**
     * Creates a new instance of this meta-type.
     *
     * @param argNames The names of the meta-type's arguments
     * @param argTypes The types of the meta-type's arguments
     */
    ChannelEvents(final String[] argNames, final Class ... argTypes) {
        this.argNames = argNames;
        this.argTypes = argTypes;
    }

    /** {@inheritDoc} */
    @Override
    public int getArity() {
        return argNames.length;
    }

    /** {@inheritDoc} */
    @Override
    public Class[] getArgTypes() {
        return argTypes;
    }

    /** {@inheritDoc} */
    @Override
    public String[] getArgNames() {
        return argNames;
    }

    /** {@inheritDoc} */
    @Override
    public String getGroup() {
        return "Channel Events";
    }

}
