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

import com.dmdirc.Server;
import com.dmdirc.actions.interfaces.ActionMetaType;

import com.dmdirc.addons.dcc.DCCChatWindow;
import com.dmdirc.addons.dcc.DCCSendWindow;

import java.io.File;

/**
 * Defines DCC-related events.
 *
 * @author Chris
 */
public enum DCCEvents implements ActionMetaType {

    /** DCC Chat Request. */
    DCC_CHAT_REQUEST(new String[]{"server", "client"}, Server.class, String.class),
    /** DCC Chat Request Sent. */
    DCC_CHAT_REQUEST_SENT(new String[]{"server", "client"}, Server.class, String.class),
    /** DCC Message from another person. */
    DCC_CHAT_MESSAGE(new String[]{"DCCChatWindow", "Nickname", "Message"}, DCCChatWindow.class, String.class, String.class),
    /** DCC Message to another person. */
    DCC_CHAT_SELFMESSAGE(new String[]{"DCCChatWindow", "Message"}, DCCChatWindow.class, String.class),
    /** DCC Chat Socket Closed. */
    DCC_CHAT_SOCKETCLOSED(new String[]{"DCCChatWindow"}, DCCChatWindow.class),
    /** DCC Chat Socket Opened. */
    DCC_CHAT_SOCKETOPENED(new String[]{"DCCChatWindow"}, DCCChatWindow.class),
    /** DCC Send Socket Closed. */
    DCC_SEND_SOCKETCLOSED(new String[]{"DCCSendWindow"}, DCCSendWindow.class),
    /** DCC Send Socket Opened. */
    DCC_SEND_SOCKETOPENED(new String[]{"DCCSendWindow"}, DCCSendWindow.class),
    /** DCC Send Data Transfered */
    DCC_SEND_DATATRANSFERED(new String[]{"DCCSendWindow", "Bytes Transfered"}, DCCSendWindow.class, int.class),
    /** DCC Send Request. */
    DCC_SEND_REQUEST(new String[]{"server", "client", "file"}, Server.class, String.class, String.class),
    /** DCC Send Request Sent. */
    DCC_SEND_REQUEST_SENT(new String[]{"server", "client", "file"}, Server.class, String.class, File.class);

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
    DCCEvents(final String[] argNames, final Class... argTypes) {
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
        return "DCC Events";
    }

}
