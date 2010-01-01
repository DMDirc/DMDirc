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

package com.dmdirc.addons.ui_dummy;

import com.dmdirc.Channel;
import com.dmdirc.commandparser.parsers.ChannelCommandParser;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.ui.interfaces.ChannelWindow;

import java.util.Collection;

/**
 * Dummy channel window, used for testing.
 */
public final class DummyChannelWindow extends DummyInputWindow implements ChannelWindow {

    /** Parent channel. */
    private final Channel parent;

    /** 
     * Instantiates a new DummyChannelWindow. 
     *
     * @param parent Parent channel
     */
    public DummyChannelWindow(final Channel parent) {
        super(parent, new ChannelCommandParser(parent.getServer(), parent));
        this.parent = parent;
    }

    /** {@inheritDoc} */
    @Override
    public void updateNames(final Collection<ChannelClientInfo> clients) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void addName(final ChannelClientInfo client) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void removeName(final ChannelClientInfo client) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void updateNames() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public Channel getChannel() {
        return parent;
    }

    /** {@inheritDoc} */
    @Override
    public void redrawNicklist() {
        // Do nothing
    }

}
