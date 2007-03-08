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

package uk.org.ownage.dmdirc.ui.framemanager;

import java.awt.Color;
import javax.swing.JComponent;
import uk.org.ownage.dmdirc.Channel;
import uk.org.ownage.dmdirc.FrameContainer;
import uk.org.ownage.dmdirc.Query;
import uk.org.ownage.dmdirc.Raw;
import uk.org.ownage.dmdirc.Server;

/**
 * A dummy frame manager. Does nothing.
 * @author chris
 */
public class DummyFrameManager implements FrameManager {
    
    /** Creates a new instance of DummyFrameManager */
    public DummyFrameManager() {
    }
    
    /**
     * Sets the parent component of this frame manager. The frame manager
     * should render itself within the parent.
     * @param parent The parent control
     */
    public void setParent(JComponent parent) {
        parent.setBackground(Color.RED);
    }
    
    /**
     * Adds a new server instance to this frame manager.
     * @param server The server to be added
     */
    public void addServer(Server server) {
        System.out.println("DummyFrameManager: addServer: "+server);
    }
    
    /**
     * Removes a server instance from this frame manager.
     * @param server The server to be removed
     */
    public void delServer(Server server) {
        System.out.println("DummyFrameManager: delServer: "+server);
    }
    
    /**
     * Adds a new channel instance to this frame manager.
     * @param server The server to which the channel belongs
     * @param channel The channel to be added
     */
    public void addChannel(Server server, Channel channel) {
        System.out.println("DummyFrameManager: addChannel: "+channel+"@"+server);
    }
    
    /**
     * Removes a channel instance from this frame manager.
     * @param server The server to which the channel belongs
     * @param channel The channel to be removed
     */
    public void delChannel(Server server, Channel channel) {
        System.out.println("DummyFrameManager: delChannel: "+channel+"@"+server);
    }
    
    /**
     * Adds a new query instance to this frame manager.
     * @param server The server to which the query belongs
     * @param query The query to be added
     */
    public void addQuery(Server server, Query query) {
        System.out.println("DummyFrameManager: addQuery: "+query+"@"+server);
    }
    
    /**
     * Removes a query instance from this frame manager.
     * @param server The server to which the query belongs
     * @param query The query to be removed
     */
    public void delQuery(Server server, Query query) {
        System.out.println("DummyFrameManager: delQuery: "+query+"@"+server);
    }
    
    /**
     * Adds a new raw instance to this frame manager.
     * @param server The server to which the raw frame belongs
     * @param raw The raw instance to be added
     */
    public void addRaw(Server server, Raw raw) {
        System.out.println("DummyFrameManager: addRaw: "+raw+"@"+server);
    }
    
    /**
     * Removes a raw instance from this frame manager.
     * @param server The server to which the raw frame belongs
     * @param raw The raw instance to be removed
     */
    public void delRaw(Server server, Raw raw) {
        System.out.println("DummyFrameManager: delRaw: "+raw+"@"+server);
    }
    
    /**
     * Indicates whether this frame manager can be positioned vertically
     * (i.e., at the side of the screen)
     * @return True iff the frame manager can be positioned vertically
     */
    public boolean canPositionVertically() {
        return true;
    }
    
    /**
     * Indicates whether this frame manager can be positioned horizontally
     * (i.e., at the top or bottom of the screen)
     * @return True iff the frame manager can be positioned horizontally
     */
    public boolean canPositionHorizontally() {
        return true;
    }
    
    /**
     * Shows an event notification to the user by colouring the corresponding
     * element to the source a specific colour
     * @param source The object requesting notification
     * @param colour The colour that should be used to indicate the notification
     */
    public void showNotification(FrameContainer source, Color colour) {
        System.out.println("DummyFrameManager: Notifcation for "+source);
    }
    
    /**
     * Removes the notification status of the specified object
     * @param source The object whose notification should be cleared
     */
    public void clearNotification(FrameContainer source) {
        System.out.println("DummyFrameManager: Clear notification for "+source);
    }
    
    /**
     * Indicates that there is a new active frame
     * @param source The object that now has focus
     */
    public void setSelected(FrameContainer source) {
        System.out.println("DummyFrameManager: Now foucsed: "+source);
    }
}
