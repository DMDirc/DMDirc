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

package com.dmdirc;

import com.dmdirc.identities.ConfigManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.MainFrame;
import com.dmdirc.ui.interfaces.Window;

import java.awt.Color;
import java.beans.PropertyVetoException;

import javax.swing.Icon;

/**
 * The frame container implements basic methods that should be present in
 * all objects that handle a frame.
 *
 * @author chris
 */
public abstract class FrameContainer {
    
    /** The colour of our frame's notifications. */
    protected Color notification = Color.BLACK;
    
    /** The icon being used for this container's frame. */
    protected Icon icon;
    
    /** Instantiate new frame container. */
    public FrameContainer() {
        //Do nothing
    }
    
    /**
     * Returns the internal frame associated with this object.
     *
     * @return The internal frame associated with this object
     */
    public abstract Window getFrame();
    
    /**
     * Returns a string identifier for this object/its frame.
     *
     * @return String identifier
     */
    public abstract String toString();
    
    /**
     * Closes this container (and it's associated frame).
     */
    public abstract void close();
    
    /**
     * Returns the server instance associated with this container.
     *
     * @return the associated server connection
     */
    public abstract Server getServer();
    
    /**
     * Retrieves the icon used by this container's window.
     *
     * @return This container's icon
     */
    public Icon getIcon() {
        return icon;
    }
    
    /**
     * Returns the config manager for this container.
     *
     * @return the associated config manager
     */
    public ConfigManager getConfigManager() {
        return getServer().getConfigManager();
    }
    
    /**
     * Requests that this object's frame be activated.
     */
    public void activateFrame() {
        MainFrame.getMainFrame().setActiveFrame(getFrame());
    }
    
    /**
     * Clears any outstanding notifications this frame has set.
     */
    protected void clearNotification() {
        notification = Color.BLACK;
        MainFrame.getMainFrame().getFrameManager().clearNotification(this);
    }
    
    /**
     * Sends a notification to the frame manager if this fame isn't active.
     *
     * @param colour The colour to use for the notification
     */
    public void sendNotification(final Color colour) {
        final Window activeFrame = MainFrame.getMainFrame().getActiveFrame();
        if (activeFrame != null && !activeFrame.equals(getFrame())) {
            notification = colour;
            MainFrame.getMainFrame().getFrameManager().showNotification(this, colour);
        }
    }
    
    /**
     * Retrieves the current notification colour of this channel.
     *
     * @return This channel's notification colour
     */
    public Color getNotification() {
        return notification;
    }
    
    /**
     * Determines if the specified frame is owned by this object.
     *
     * @param target Window to check ownership of
     * @return True iff frame is owned by this container, false otherwise
     */
    public boolean ownsFrame(final Window target) {
        return getFrame().equals(target);
    }
    
    /**
     * Invoked when our window has been opened.
     */
    public void windowOpened() {
        final boolean pref = getServer().getConfigManager().getOptionBool("ui", "maximisewindows");
        if (pref || MainFrame.getMainFrame().getMaximised()) {
            try {
                getFrame().setMaximum(true);
            } catch (PropertyVetoException ex) {
                Logger.userError(ErrorLevel.LOW, "Unable to maximise window");
            }
        }
    }
    
    /**
     * Invoked when our window is closing.
     */
    public void windowClosing() {
        close();
    }
    
    /**
     * Invoked when our window has been closed.
     */
    public void windowClosed() {
        // Ignore.
    }
    
    /**
     * Invoked when our window is activated.
     */
    public void windowActivated() {
        if (MainFrame.getMainFrame().getMaximised()) {
            try {
                getFrame().setMaximum(true);
            } catch (PropertyVetoException ex) {
                Logger.userError(ErrorLevel.LOW, "Unable to maximise window");
            }
        }
        MainFrame.getMainFrame().getFrameManager().setSelected(this);
        getServer().setActiveFrame(this);
        clearNotification();
    }
    
    /**
     * Invoked when our window is deactivated.
     */
    public void windowDeactivated() {
        // Do nothing.
    }
    
}
