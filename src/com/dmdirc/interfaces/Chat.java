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

package com.dmdirc.interfaces;

import com.dmdirc.FrameContainer;
import com.dmdirc.parser.common.CompositionState;

import java.util.Optional;

/**
 * A generic chat, to which the user can send messages.
 */
public interface Chat {

    /**
     * Gets the connection that this chat is happening on.
     *
     * @return This chat's connection.
     */
    Optional<Connection> getConnection();

    /**
     * Sends an action to the chat. If an action is too long to be sent, an error will be displayed.
     *
     * @param action The action to be sent.
     */
    void sendAction(final String action);

    /**
     * Sends a line of text to the chat. If a line is too long to be sent, it will be split and sent
     * as multiple lines.
     *
     * @param line The line to be sent.
     */
    void sendLine(final String line);

    /**
     * Gets the maximum length of a line that may be sent to this chat.
     *
     * @return The maximum line length that may be sent.
     */
    int getMaxLineLength();

    /**
     * Sets the composition state for the local user for this chat.
     *
     * @param state The new composition state
     */
    void setCompositionState(final CompositionState state);

    /**
     * Gets the core model for the input/output window for this connection.
     *
     * @return A model for windows based on this connection.
     */
    FrameContainer getWindowModel();

}
