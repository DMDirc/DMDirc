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

import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.input.InputHandler;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.util.StringTranscoder;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Dummy input window, used for testing.
 */
public class DummyInputWindow implements InputWindow {
    
    /** Window title. */
    private String title;
    /** Are we visible? */
    private boolean visible;
    /** are we maximised? */
    private boolean maximised;
    /** Our container. */
    private final WritableFrameContainer container;
    /** Our command parser. */
    private final CommandParser commandParser;
    
    /** 
     * Instantiates a new DummyInputWindow.
     * 
     * @param owner Parent window
     * @param commandParser Parent command parser
     */
    public DummyInputWindow(final WritableFrameContainer owner, 
            final CommandParser commandParser) {
        this.container = owner;
        this.commandParser = commandParser;
    }
    
    /** {@inheritDoc} */
    @Override
    public CommandParser getCommandParser() {
        return commandParser;
    }
    
    /** {@inheritDoc} */
    @Override
    public InputHandler getInputHandler() {
        return new DummyInputHandler(new DummyInputField(), null, this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setAwayIndicator(final boolean isAway) {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void addLine(final String messageType, final Object... args) {
        System.out.println("DummyInputWindow.addLine(" + messageType + ", " + Arrays.toString(args) + ")");
    }
    
    /** {@inheritDoc} */
    @Override
    public void addLine(final StringBuffer messageType, final Object... args) {
        addLine(messageType.toString(), args);
    }
    
    /** {@inheritDoc} */
    @Override
    public void addLine(final String line, final boolean timestamp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /** {@inheritDoc} */
    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /** {@inheritDoc} */
    @Override
    public ConfigManager getConfigManager() {
        return IdentityManager.getGlobalConfig();
    }
    
    /** {@inheritDoc} */
    @Override
    public WritableFrameContainer getContainer() {
        return container;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isVisible() {
        return visible;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setVisible(final boolean isVisible) {
        visible = isVisible;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return title;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isMaximum() {
        return maximised;
    }
    
    /** 
     * {@inheritDoc} 
     * 
     * @param b maximised or not
     */
    public void setMaximum(final boolean b) {
        maximised = b;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setTitle(final String title) {
        this.title = title;
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
        container.windowClosing();
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
