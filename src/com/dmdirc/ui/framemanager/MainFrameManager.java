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

package com.dmdirc.ui.framemanager;

import com.dmdirc.Channel;
import com.dmdirc.Config;
import com.dmdirc.FrameContainer;
import com.dmdirc.Query;
import com.dmdirc.Server;
import com.dmdirc.ui.framemanager.buttonbar.ButtonBar;
import com.dmdirc.ui.framemanager.tree.TreeFrameManager;
import com.dmdirc.ui.framemanager.windowmenu.WindowMenuFrameManager;

import java.awt.Color;

import javax.swing.JComponent;

/**
 * Instantiates and manages the active frame managers.
 */
public final class MainFrameManager implements FrameManager {
    
    /** Active frame manager. */
    private final FrameManager frameManager;
    
    /** Window menu frame manager. */
    private final WindowMenuFrameManager windowMenuFrameManager;
    
    /** Creates a new instance of MainFrameManager. */
    public MainFrameManager() {
        final String manager = Config.getOption("ui", "framemanager", "treeview");
        
        if (manager.equalsIgnoreCase("buttonbar")) {
            frameManager = new ButtonBar();
        } else {
            frameManager = new TreeFrameManager();
        }
        
        windowMenuFrameManager = new WindowMenuFrameManager();
    }
    
    /** {@inheritDoc} */
    public void setParent(final JComponent parent) {
        frameManager.setParent(parent);
    }
    
    /** {@inheritDoc} */
    public boolean canPositionVertically() {
        return frameManager.canPositionVertically();
    }
    
    /** {@inheritDoc} */
    public boolean canPositionHorizontally() {
        return frameManager.canPositionHorizontally();
    }
    
    /** {@inheritDoc} */
    public void setSelected(final FrameContainer source) {
        frameManager.setSelected(source);
        windowMenuFrameManager.setSelected(source);
    }
    
    /** {@inheritDoc} */
    public void showNotification(final FrameContainer source, final Color colour) {
        frameManager.showNotification(source, colour);
        windowMenuFrameManager.showNotification(source, colour);
    }
    
    /** {@inheritDoc} */
    public void clearNotification(final FrameContainer source) {
        frameManager.clearNotification(source);
        windowMenuFrameManager.clearNotification(source);
    }
    
    /** {@inheritDoc} */
    public void addServer(final Server server) {
        frameManager.addServer(server);
        windowMenuFrameManager.addServer(server);
    }
    
    /** {@inheritDoc} */
    public void delServer(final Server server) {
        frameManager.delServer(server);
        windowMenuFrameManager.delServer(server);
    }
    
    /** {@inheritDoc} */
    public void addChannel(final Server server, final Channel channel) {
        frameManager.addChannel(server, channel);
        windowMenuFrameManager.addChannel(server, channel);
    }
    
    /** {@inheritDoc} */
    public void delChannel(final Server server, final Channel channel) {
        frameManager.delChannel(server, channel);
        windowMenuFrameManager.delChannel(server, channel);
    }
    
    /** {@inheritDoc} */
    public void addQuery(final Server server, final Query query) {
        frameManager.addQuery(server, query);
        windowMenuFrameManager.addQuery(server, query);
    }
    
    /** {@inheritDoc} */
    public void delQuery(final Server server, final Query query) {
        frameManager.delQuery(server, query);
        windowMenuFrameManager.delQuery(server, query);
    }
    
    /** {@inheritDoc} */
    public void addCustom(final Server server, final FrameContainer window) {
        frameManager.addCustom(server, window);
        windowMenuFrameManager.addCustom(server, window);
    }
    
    /** {@inheritDoc} */
    public void delCustom(final Server server, final FrameContainer window) {
        frameManager.delCustom(server, window);
        windowMenuFrameManager.delCustom(server, window);
    }
    
    /**
     * Returns the active frame manager.
     *
     * @return Active frame manage
     */
    public FrameManager getActiveFrameManager() {
        return frameManager;
    }
    
}
