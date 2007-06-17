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

package com.dmdirc.ui.framemanager.windowmenu;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.Query;
import com.dmdirc.Server;
import com.dmdirc.ui.MainFrame;
import com.dmdirc.ui.framemanager.FrameManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

/**
 * Manages the window menu window list.
 */
public final class WindowMenuFrameManager implements FrameManager, ActionListener {
    
    /** Menu item list. */
    private final Map<FrameContainer, JMenuItem> menuItemMap;
    
    /** Creates a new instance of WindowMenuFrameManager. */
    public WindowMenuFrameManager() {
        menuItemMap = new HashMap<FrameContainer, JMenuItem>();
    }
    
    /** {@inheritDoc} */
    public void setParent(final JComponent parent) {
        //Ignore
    }
    
    /** {@inheritDoc} */
    public boolean canPositionVertically() {
        return true;
    }
    
    /** {@inheritDoc} */
    public boolean canPositionHorizontally() {
        return true;
    }
    
    /** {@inheritDoc} */
    public void setSelected(final FrameContainer source) {
        synchronized (menuItemMap) {
            for (Entry<FrameContainer, JMenuItem> entry : menuItemMap.entrySet()) {
                final JMenuItem mi = entry.getValue();
                if (entry.getKey() == source) {
                    mi.setFont(mi.getFont().deriveFont(Font.BOLD));
                } else {
                    mi.setFont(mi.getFont().deriveFont(Font.PLAIN));
                }
            }
        }
    }
    
    /** {@inheritDoc} */
    public void showNotification(final FrameContainer source, final Color colour) {
        //Ignore
    }
    
    /** {@inheritDoc} */
    public void clearNotification(final FrameContainer source) {
        //Ignore
    }
    
    /** {@inheritDoc} */
    public void addServer(final Server server) {
        addFrameContainer(server);
    }
    
    /** {@inheritDoc} */
    public void delServer(final Server server) {
        removeFramecontainer(server);
    }
    
    /** {@inheritDoc} */
    public void addChannel(final Server server, final Channel channel) {
        addFrameContainer(channel);
    }
    
    /** {@inheritDoc} */
    public void delChannel(final Server server, final Channel channel) {
        removeFramecontainer(channel);
    }
    
    /** {@inheritDoc} */
    public void addQuery(final Server server, final Query query) {
        addFrameContainer(query);
    }
    
    /** {@inheritDoc} */
    public void delQuery(final Server server, final Query query) {
        removeFramecontainer(query);
    }
    
    /** {@inheritDoc} */
    public void addCustom(final Server server, final FrameContainer window) {
        addFrameContainer(window);
    }
    
    /** {@inheritDoc} */
    public void delCustom(final Server server, final FrameContainer window) {
        removeFramecontainer(window);
    }
    
    /** 
     * Adds a frame container to the list. 
     *
     * @param window Window to add to the list
     */
    private void addFrameContainer(final FrameContainer window) {
        final JMenuItem mi = new JMenuItem(window.toString());
        mi.addActionListener(this);
        menuItemMap.put(window, mi);
        MainFrame.getMainFrame().populateWindowMenu(menuItemMap);
    }
    
    /** 
     * Removes a frame container from the list. 
     *
     * @param window Window to remove from list
     */
    private void removeFramecontainer(final FrameContainer window) {
        menuItemMap.remove(window);
        MainFrame.getMainFrame().populateWindowMenu(menuItemMap);
    }
    
    /** {@inheritDoc} */
    public void actionPerformed(final ActionEvent e) {
        synchronized (menuItemMap) {
            for (Entry<FrameContainer, JMenuItem> entry : menuItemMap.entrySet()) {
                if (entry.getValue() == e.getSource()) {
                    entry.getKey().activateFrame();
                }
            }
        }
    }
    
}
