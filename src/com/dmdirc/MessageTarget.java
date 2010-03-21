/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.config.ConfigManager;
import com.dmdirc.ui.interfaces.InputWindow;

/**
 * Defines common methods for objects that you can send messages to (such as
 * channels and queries).
 *
 * @param <T> The type of window which should be used for this frame container.
 * @author Chris
 */
public abstract class MessageTarget<T extends InputWindow> extends WritableFrameContainer<T> {

    /**
     * Creates a new MessageTarget.
     * 
     * @param icon The icon to use for this target
     * @param name The name of this target
     * @param title The title of this target
     * @param windowClass The class of window to use to represent this container
     * @param config The config manager to use for this target
     * @param parser The command parser for this container
     * @since 0.6.4
     */
    public MessageTarget(final String icon, final String name,
            final String title, final Class<T> windowClass,
            final ConfigManager config, final CommandParser parser) {
        super(icon, name, title, windowClass, config, parser);
    }

    /**
     * Sends the specified string as an action (CTCP) to the target that this
     * object represents.
     *
     * @param action The action to send
     */
    public abstract void sendAction(final String action);

}
