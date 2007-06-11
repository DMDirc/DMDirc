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
import com.dmdirc.FrameContainer;
import com.dmdirc.Query;
import com.dmdirc.Server;
import com.dmdirc.ui.MainFrame;

import java.awt.Color;

import javax.swing.JComponent;

/**
 * A dummy frame manager. Does nothing.
 * @author chris
 */
public final class DummyFrameManager implements FrameManager {
    
    /** Creates a new instance of DummyFrameManager. */
    public DummyFrameManager() {
        //Do nothing.
    }
    
    /** {@inheritDoc} */
    public void setParent(final JComponent parent) {
        parent.setBackground(Color.RED);
    }
    
    /** {@inheritDoc} */
    public void addServer(final Server server) {
        MainFrame.getMainFrame().getStatusBar()
                .setMessage("DummyFrameManager: addServer: " + server);
    }
    
    /** {@inheritDoc} */
    public void delServer(final Server server) {
        MainFrame.getMainFrame().getStatusBar()
                .setMessage("DummyFrameManager: delServer: " + server);
    }
    
    /** {@inheritDoc} */
    public void addChannel(final Server server, final Channel channel) {
        MainFrame.getMainFrame().getStatusBar()
                .setMessage("DummyFrameManager: addChannel: " + channel + "@" + server);
    }
    
    /** {@inheritDoc} */
    public void delChannel(final Server server, final Channel channel) {
        MainFrame.getMainFrame().getStatusBar()
                .setMessage("DummyFrameManager: delChannel: " + channel + "@" + server);
    }
    
    /** {@inheritDoc} */
    public void addQuery(final Server server, final Query query) {
        MainFrame.getMainFrame().getStatusBar()
                .setMessage("DummyFrameManager: addQuery: " + query + "@" + server);
    }
    
    /** {@inheritDoc} */
    public void delQuery(final Server server, final Query query) {
        MainFrame.getMainFrame().getStatusBar()
                .setMessage("DummyFrameManager: delQuery: " + query + "@" + server);
    }
    
    /** {@inheritDoc} */
    public void addCustom(final Server server, final FrameContainer window) {
        MainFrame.getMainFrame().getStatusBar()
                .setMessage("DummyFrameManager: addCustom: " + window + "@" + server);
    }
    
    /** {@inheritDoc} */
    public void delCustom(final Server server, final FrameContainer window) {
        MainFrame.getMainFrame().getStatusBar()
                .setMessage("DummyFrameManager: delCustom: " + window + "@" + server);
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
    public void showNotification(final FrameContainer source, final Color colour) {
        MainFrame.getMainFrame().getStatusBar()
                .setMessage("DummyFrameManager: Notifcation for " + source);
    }
    
    /** {@inheritDoc} */
    public void clearNotification(final FrameContainer source) {
        MainFrame.getMainFrame().getStatusBar()
                .setMessage("DummyFrameManager: Clear notification for " + source);
    }
    
    /** {@inheritDoc} */
    public void setSelected(final FrameContainer source) {
        MainFrame.getMainFrame().getStatusBar()
                .setMessage("DummyFrameManager: Now focused: " + source);
    }
}
