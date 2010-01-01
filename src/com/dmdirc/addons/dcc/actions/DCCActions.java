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

package com.dmdirc.addons.dcc.actions;

import com.dmdirc.actions.interfaces.ActionMetaType;
import com.dmdirc.actions.interfaces.ActionType;

/**
 * DCC actions.
 *
 * @author chris
 */
public enum DCCActions implements ActionType {

    /** DCC Chat Request. */
    DCC_CHAT_REQUEST(DCCEvents.DCC_CHAT_REQUEST, "DCC Chat Requested"),
    /** DCC Chat Request Sent. */
    DCC_CHAT_REQUEST_SENT(DCCEvents.DCC_CHAT_REQUEST_SENT, "DCC Chat Request Sent"),
    /** DCC Message from another person. */
    DCC_CHAT_MESSAGE(DCCEvents.DCC_CHAT_MESSAGE, "DCC Chat Message Recieved"),
    /** DCC Message to another person. */
    DCC_CHAT_SELFMESSAGE(DCCEvents.DCC_CHAT_SELFMESSAGE, "DCC Chat Message Sent"),
    /** DCC Chat Socket Closed. */
    DCC_CHAT_SOCKETCLOSED(DCCEvents.DCC_CHAT_SOCKETCLOSED, "DCC Chat Socket Closed"),
    /** DCC Chat Socket Opened. */
    DCC_CHAT_SOCKETOPENED(DCCEvents.DCC_CHAT_SOCKETOPENED, "DCC Chat Socket Opened"),
    /** DCC Send Socket Closed. */
    DCC_SEND_SOCKETCLOSED(DCCEvents.DCC_SEND_SOCKETCLOSED, "DCC Send Socket Closed"),
    /** DCC Send Socket Opened. */
    DCC_SEND_SOCKETOPENED(DCCEvents.DCC_SEND_SOCKETOPENED, "DCC Send Socket Opened"),
    /** DCC Send Data Transfered */
    DCC_SEND_DATATRANSFERED(DCCEvents.DCC_SEND_DATATRANSFERED, "DCC Send Socket Closed"),
    /** DCC Send Request. */
    DCC_SEND_REQUEST(DCCEvents.DCC_SEND_REQUEST, "DCC Chat Requested"),
    /** DCC Send Request Sent. */
    DCC_SEND_REQUEST_SENT(DCCEvents.DCC_SEND_REQUEST_SENT, "DCC Chat Request Sent");

    /** The type of this action. */
    private final ActionMetaType type;

    /** The name of this action. */
    private final String name;

    /**
     * Constructs a new core action.
     * @param type The type of this action
     * @param name The name of this action
     */
    DCCActions(final ActionMetaType type, final String name) {
        this.type = type;
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public ActionMetaType getType() {
        return type;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

}
