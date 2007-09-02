/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.ui.dummy;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.StringTranscoder;
import com.dmdirc.commandparser.ChannelCommandParser;
import com.dmdirc.commandparser.CommandParser;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.parser.ChannelClientInfo;
import com.dmdirc.ui.input.InputHandler;
import com.dmdirc.ui.interfaces.ChannelWindow;
import java.beans.PropertyVetoException;
import java.util.Arrays;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JTextField;

/**
 *
 * @author Chris
 */
public class DummyChannelWindow implements ChannelWindow {
    
    private final Channel parent;
    
    private String title;

    public DummyChannelWindow(final Channel parent) {
        this.parent = parent;
    }

    /** {@inheritDoc} */
    public void updateNames(final List<ChannelClientInfo> clients) {
        // Do nothing
    }

    public void addName(final ChannelClientInfo client) {
        // Do nothing
    }

    /** {@inheritDoc} */
    public void removeName(final ChannelClientInfo client) {
        // Do nothing
    }

    /** {@inheritDoc} */
    public void updateNames() {
        // Do nothing
    }

    /** {@inheritDoc} */
    public CommandParser getCommandParser() {
        return new ChannelCommandParser(parent.getServer(), parent);
    }

    /** {@inheritDoc} */
    public InputHandler getInputHandler() {
        return new InputHandler(new JTextField(), getCommandParser(), this);
    }

    public void setAwayIndicator(boolean isAway) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    public void addLine(final String messageType, final Object... args) {
        System.out.println("DummyChannelWindow.addLine(" + messageType + ", " + Arrays.toString(args) + ")");
    }
    
    /** {@inheritDoc} */
    public void addLine(final StringBuffer messageType, final Object... args) {
        addLine(messageType.toString(), args);
    }

    public void addLine(String line, boolean timestamp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    public ConfigManager getConfigManager() {
        return parent.getConfigManager();
    }

    /** {@inheritDoc} */
    public FrameContainer getContainer() {
        return parent;
    }

    public boolean isVisible() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setVisible(boolean isVisible) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    public String getTitle() {
        return title;
    }

    public boolean isMaximum() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMaximum(boolean b) throws PropertyVetoException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    public void setTitle(String title) {
        this.title = title;
    }

    /** {@inehritDoc} */
    public void open() {
        // Do nothing
    }

    /** {@inheritDoc} */
    public void setFrameIcon(final Icon icon) {
        // Do nothing
    }

    public StringTranscoder getTranscoder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
