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

package com.dmdirc.addons.redirect;

import com.dmdirc.MessageTarget;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.ui.input.InputHandler;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.messages.Formatter;
import com.dmdirc.util.StringTranscoder;

import java.beans.PropertyVetoException;
import java.nio.charset.Charset;

/**
 * Implements a fake input window, which sends echoed text to the specified
 * chat window instead.
 * 
 * @author Chris
 */
public class FakeInputWindow implements InputWindow {
    
    /** The target for this window. */
    private final MessageTarget target;

    /**
     * Creates a new instance of FakeInputWindow.
     * 
     * @param target The message target that output gets sent to
     */
    public FakeInputWindow(final MessageTarget target) {
        this.target = target;
    }

    /** {@inheritDoc} */
    @Override
    public CommandParser getCommandParser() {
        return target.getFrame().getCommandParser();
    }

    /** {@inheritDoc} */
    @Override
    public InputHandler getInputHandler() {
        return target.getFrame().getInputHandler();
    }

    /** {@inheritDoc} */
    @Override
    public void setAwayIndicator(final boolean isAway) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void addLine(final String messageType, final Object... args) {
        target.sendLine(Formatter.formatMessage(getConfigManager(), messageType, args));
    }

    /** {@inheritDoc} */
    @Override
    public void addLine(final StringBuffer messageType, final Object... args) {
        addLine(messageType.toString(), args);
    }

    /** {@inheritDoc} */
    @Override
    public void addLine(final String line, final boolean timestamp) {
        target.sendLine(line);
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public ConfigManager getConfigManager() {
        return target.getFrame().getConfigManager();
    }

    /** {@inheritDoc} */
    @Override
    public WritableFrameContainer getContainer() {
        return target;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isVisible() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(final boolean isVisible) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return "Fake window";
    }

    /** {@inheritDoc} */
    @Override
    public boolean isMaximum() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void setTitle(final String title) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void open() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public StringTranscoder getTranscoder() {
        return new StringTranscoder(Charset.defaultCharset());
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        /// Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void restore() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void maximise() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void toggleMaximise() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void minimise() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void activateFrame() {
        // Do nothing
    }

}
