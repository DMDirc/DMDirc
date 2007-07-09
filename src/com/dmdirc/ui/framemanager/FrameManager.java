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

import java.awt.Color;

import javax.swing.JComponent;

/**
 * A frame manager is a widget that allows the user to navigate between the
 * various frames that will be open at any one time.
 * 
 * @author chris
 */
public interface FrameManager {
    
    /**
     * Sets the parent component of this frame manager. The frame manager
     * should render itself within the parent.
     * 
     * @param parent The parent control
     */
    void setParent(JComponent parent);
    
    /**
     * Indicates whether this frame manager can be positioned vertically
     * (i.e., at the side of the screen).
     * 
     * @return True iff the frame manager can be positioned vertically
     */
    boolean canPositionVertically();
    
    /**
     * Indicates whether this frame manager can be positioned horizontally
     * (i.e., at the top or bottom of the screen).
     * 
     * @return True iff the frame manager can be positioned horizontally
     */
    boolean canPositionHorizontally();
    
    /**
     * Indicates that there is a new active frame.
     * 
     * @param source The object that now has focus
     */
    void setSelected(FrameContainer source);
    
    /**
     * Shows an event notification to the user by colouring the corresponding
     * element to the source a specific colour.
     * 
     * @param source The object requesting notification
     * @param colour The colour that should be used to indicate the notification
     */
    void showNotification(FrameContainer source, Color colour);
    
    /**
     * Removes the notification status of the specified object.
     * 
     * @param source The object whose notification should be cleared
     */
    void clearNotification(FrameContainer source);
    
    /**
     * Adds a new server instance to this frame manager.
     * 
     * @param server The server to be added
     */
    void addServer(Server server);
    
    /**
     * Removes a server instance from this frame manager.
     * 
     * @param server The server to be removed
     */
    void delServer(Server server);
    
    /**
     * Adds a new channel instance to this frame manager.
     * 
     * @param server The server to which the channel belongs
     * @param channel The channel to be added
     */
    void addChannel(Server server, Channel channel);
    
    /**
     * Removes a channel instance from this frame manager.
     * 
     * @param server The server to which the channel belongs
     * @param channel The channel to be removed
     */
    void delChannel(Server server, Channel channel);
    
    /**
     * Adds a new query instance to this frame manager.
     * 
     * @param server The server to which the query belongs
     * @param query The query to be added
     */
    void addQuery(Server server, Query query);
    
    /**
     * Removes a query instance from this frame manager.
     * 
     * @param server The server to which the query belongs
     * @param query The query to be removed
     */
    void delQuery(Server server, Query query);
    
    /**
     * Adds a new custom window to this frame manager.
     * 
     * @param server The server to which the window belongs, or null
     * @param window The custom window to be added
     */
    void addCustom(Server server, FrameContainer window);
    
    /**
     * Removes a custom window from this frame manager.
     * 
     * @param server The server to which the window belongs, or null
     * @param window The custom window to be removed
     */
    void delCustom(Server server, FrameContainer window);
    
    /**
     * Informs the frame manager that the specified window has had its icon
     * updated.
     * 
     * @param window The window that was updated
     */
    void iconUpdated(FrameContainer window);
}
