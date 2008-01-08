/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.FrameContainer;
import com.dmdirc.Server;
import com.dmdirc.util.StringTranscoder;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.commandparser.parsers.ServerCommandParser;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.ui.input.InputHandler;
import com.dmdirc.ui.interfaces.ServerWindow;

import java.beans.PropertyVetoException;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.swing.Icon;

/**
 * Dummy server window, used for testing.
 */
public final class DummyServerWindow implements ServerWindow {
    
    /** Parent server. */
    private final Server parent;
    
    /** Are we visible? */
    private boolean visible;
    
    /** Server window title. */
    private String title;
    
    /** are we maximised? */
    private boolean maximised;
    
    /** 
     * Instantiates this DummyServerWindow. 
     *
     * @param parent Parent server
     */
    public DummyServerWindow(final Server parent) {
        this.parent = parent;
    }
    
    /** {@inheritDoc} */
    public CommandParser getCommandParser() {
        return new ServerCommandParser(parent);
    }
    
    /** {@inheritDoc} */
    public InputHandler getInputHandler() {
        return new InputHandler(new DummyInputField(), null, this);
    }
    
    /** {@inheritDoc} */
    public void setAwayIndicator(final boolean isAway) {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    public void addLine(final String messageType, final Object... args) {
        System.out.println("DummyServerWindow.addLine(" + messageType + ", " + Arrays.toString(args) + ")");
    }
    
    /** {@inheritDoc} */
    public void addLine(final StringBuffer messageType, final Object... args) {
        addLine(messageType.toString(), args);
    }
    
    /** {@inheritDoc} */
    public void addLine(final String line, final boolean timestamp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /** {@inheritDoc} */
    public void clear() {
        System.out.println("DummyServerWindow.clear()");
    }
    
    /** {@inheritDoc} */
    public ConfigManager getConfigManager() {
        return new ConfigManager("dummy", "dummy", "dummy");
    }
    
    /** {@inheritDoc} */
    public FrameContainer getContainer() {
        return parent;
    }
    
    /** {@inheritDoc} */
    public boolean isVisible() {
        return visible;
    }
    
    /** {@inheritDoc} */
    public void setVisible(final boolean isVisible) {
        visible = isVisible;
    }
    
    /** {@inheritDoc} */
    public String getTitle() {
        return title;
    }
    
    /** {@inheritDoc} */
    public boolean isMaximum() {
        return maximised;
    }
    
    /** {@inheritDoc} */
    public void setMaximum(final boolean b) throws PropertyVetoException {
        maximised = b;
    }
    
    /** {@inheritDoc} */
    public void setTitle(final String title) {
        this.title = title;
    }
    
    /** {@inheritDoc} */
    public void open() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    public void setFrameIcon(final Icon icon) {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    public StringTranscoder getTranscoder() {
        return new StringTranscoder(Charset.defaultCharset());
    }
    
    /** {@inheritDoc} */
    @Override
    public void close() {
        parent.windowClosing();
    }    
    
}
