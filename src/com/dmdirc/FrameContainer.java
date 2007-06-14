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
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.MainFrame;
import com.dmdirc.ui.interfaces.Window;

import java.awt.Color;
import java.beans.PropertyVetoException;

import javax.swing.ImageIcon;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * The frame container implements basic methods that should be present in
 * all objects that handle a frame.
 * 
 * @author chris
 */
public abstract class FrameContainer implements InternalFrameListener {
    
    /** The colour of our frame's notifications. */
    protected Color notification = Color.BLACK;
    
    /** The icon being used for this container's frame. */
    protected ImageIcon imageIcon;
    
    /**
     * Returns the internal frame associated with this object.
     * 
     * @return The internal frame associated with this object
     */
    public abstract Window getFrame();
    
    /**
     * Returns a string identifier for this object/its frame.
     * @return String identifier
     */
    public abstract String toString();
    
    /**
     * Closes this container (and it's associated frame).
     */
    public abstract void close();
    
    /**
     * Returns the server instance associated with this container.
     * @return the associated server connection
     */
    public abstract Server getServer();
    
    /**
     * Retrieves the icon used by the query frame.
     * @return The query frame's icon
     */
    public ImageIcon getIcon() {
        return imageIcon;
    }
    
    /**
     * Returns the config manager for this container.
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
        MainFrame.getMainFrame().getFrameManager().clearNotification(this);
        notification = Color.BLACK;
    }
    
    /**
     * Sends a notification to the frame manager if this fame isn't active.
     * @param colour The colour to use for the notification
     */
    public void sendNotification(final Color colour) {
        final Window activeFrame = MainFrame.getMainFrame().getActiveFrame();
        if (activeFrame != null && !activeFrame.equals(getFrame())) {
            MainFrame.getMainFrame().getFrameManager().showNotification(this, colour);
            notification = colour;
        }
    }
    
    /**
     * Retrieves the current notification colour of this channel.
     * @return This channel's notification colour
     */
    public Color getNotification() {
        return notification;
    }
    
    /**
     * Determines if the specified frame is owned by this object.
     * @param target Window to check ownership of
     * @return True iff frame is owned by this container, false otherwise
     */
    public boolean ownsFrame(final Window target) {
        return getFrame().equals(target);
    }
    
    /**
     * Invoked when a internal frame has been opened.
     * @param internalFrameEvent frame opened event
     */
    public void internalFrameOpened(final InternalFrameEvent internalFrameEvent) {
        final boolean pref = getServer().getConfigManager().getOptionBool("ui", "maximisewindows");
        if (pref || MainFrame.getMainFrame().getMaximised()) {
            try {
                getFrame().setMaximum(true);
            } catch (PropertyVetoException ex) {
                Logger.error(ErrorLevel.WARNING, "Unable to maximise window", ex);
            }
        }
    }
    
    /**
     * Invoked when an internal frame is in the process of being closed.
     * @param internalFrameEvent frame closing event
     */
    public void internalFrameClosing(final InternalFrameEvent internalFrameEvent) {
        close();
    }
    
    /**
     * Invoked when an internal frame has been closed.
     * @param internalFrameEvent frame closed event
     */
    public void internalFrameClosed(final InternalFrameEvent internalFrameEvent) {
        //Ignore.
    }
    
    /**
     * Invoked when an internal frame is iconified.
     * @param internalFrameEvent frame iconified event
     */
    public void internalFrameIconified(final InternalFrameEvent internalFrameEvent) {
        //Ignore.
    }
    
    /**
     * Invoked when an internal frame is de-iconified.
     * @param internalFrameEvent frame deiconified event
     */
    public void internalFrameDeiconified(final InternalFrameEvent internalFrameEvent) {
        //Ignore.
    }
    
    /**
     * Invoked when an internal frame is activated.
     * @param internalFrameEvent frame activation event
     */
    public void internalFrameActivated(final InternalFrameEvent internalFrameEvent) {
        if (MainFrame.getMainFrame().getMaximised()) {
            try {
                getFrame().setMaximum(true);
            } catch (PropertyVetoException ex) {
                Logger.error(ErrorLevel.WARNING, "Unable to maximise window", ex);
            }
        }
        MainFrame.getMainFrame().getFrameManager().setSelected(this);
        getServer().setActiveFrame(this);
        clearNotification();
    }
    
    /**
     * Invoked when an internal frame is de-activated.
     * @param internalFrameEvent frame deactivation event
     */
    public void internalFrameDeactivated(final InternalFrameEvent internalFrameEvent) {
        //Ignore.
    }
    
}
