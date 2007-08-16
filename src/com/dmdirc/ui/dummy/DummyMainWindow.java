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

import com.dmdirc.ui.interfaces.FrameManager;
import com.dmdirc.ui.interfaces.MainWindow;
import com.dmdirc.ui.interfaces.StatusBar;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.swing.framemanager.DummyFrameManager;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import javax.swing.JMenuItem;

/**
 *
 * @author Chris
 */
public class DummyMainWindow implements MainWindow {
    
    private final List<Window> windows = new ArrayList<Window>();
    private final List<JMenuItem> pluginMenu = new ArrayList<JMenuItem>();
    private boolean maximised = false;
    private boolean visible = true;
    
    private Window active = null;
    
    /**
     * Creates a new instance of DummyMainWindow.
     */
    public DummyMainWindow() {
    }
    
    /** {@inheritDoc} */
    public void addChild(final Window window) {
        windows.add(window);
    }
    
    /** {@inheritDoc} */
    public void delChild(final Window window) {
        windows.add(window);
    }
    
    /** {@inheritDoc} */
    public void setActiveFrame(final Window frame) {
        active = frame;
    }
    
    /** {@inheritDoc} */
    public FrameManager getFrameManager() {
        return new DummyFrameManager();
    }
    
    /** {@inheritDoc} */
    public ImageIcon getIcon() {
        return null;
    }
    
    /** {@inheritDoc} */
    public Window getActiveFrame() {
        return active;
    }
    
    /** {@inheritDoc} */
    public StatusBar getStatusBar() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /** {@inheritDoc} */
    public void addPluginMenu(final JMenuItem menuItem) {
        pluginMenu.add(menuItem);
    }
    
    /** {@inheritDoc} */
    public void removePluginMenu(final JMenuItem menuItem) {
        pluginMenu.remove(menuItem);
    }
    
    /** {@inheritDoc} */
    public void quit() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    public void setMaximised(final boolean max) {
        maximised = max;
    }
    
    /** {@inheritDoc} */
    public boolean getMaximised() {
        return maximised;
    }
    
    /** {@inheritDoc} */
    public String getTitlePrefix() {
        return "DMDirc: Dummy UI: ";
    }
    
    /** {@inheritDoc} */
    public void setTitle(final String newTitle) {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }
    
    /** {@inheritDoc} */
    public boolean isVisible() {
        return visible;
    }
    
}
