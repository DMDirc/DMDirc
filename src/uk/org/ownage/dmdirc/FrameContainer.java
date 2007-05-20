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

package uk.org.ownage.dmdirc;

import java.beans.PropertyVetoException;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import uk.org.ownage.dmdirc.commandparser.CommandWindow;
import uk.org.ownage.dmdirc.identities.ConfigManager;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.ui.MainFrame;

/**
 * Standard interface for all objects that contain an innerframe. Provides
 * methods for interfacing with the frame via the owner.
 * @author chris
 */
public abstract class FrameContainer implements InternalFrameListener {
    
    /**
     * Requests that this object's frame be activated.
     */
    public abstract void activateFrame();
    
    /**
     * Determines if this object, or any of its children, owns the specified
     * frame.
     * @param target The frame to check
     * @return true iff the frame is owned by this object, false otherwise
     */
    public abstract boolean ownsFrame(JInternalFrame target);
    
    /**
     * Sends a notification to the frame manager that this object has been
     * updated.
     */
    public abstract void sendNotification();
    
    /**
     * Returns the internal frame associated with this object.
     * @return The internal frame associated with this object
     */
    public abstract CommandWindow getFrame();
    
    /**
     * Returns a string identifier for this object/its frame.
     * @return String identifier
     */
    public abstract String toString();
    
    /**
     * Returns a reference to the icon that is currently being used by the
     * frame.
     * @return The frame's icon
     */
    public abstract ImageIcon getIcon();
    
    /**
     * Closes this container (and it's associated frame).
     */
    public abstract void close();
    
    /**
     * Returns the server instance associated with this frame.
     *
     * @return the associated server connection
     */
    public abstract Server getServer();
    
    /**
     * Returns the config manager for this frame.
     * @return the associated config manager
     */
    public abstract ConfigManager getConfigManager();
    
    /**
     * Clears any outstanding notifications this frame has set.
     */
    protected abstract void clearNotification();
    
    /**
     * Invoked when a internal frame has been opened.
     * @param internalFrameEvent frame opened event
     */
    public void internalFrameOpened(final InternalFrameEvent internalFrameEvent) {
        final boolean pref = getServer().getConfigManager().getOptionBool("ui", "maximisewindows");
        if (pref || MainFrame.getMainFrame().getMaximised()) {
            try {
                ((JInternalFrame) getFrame()).setMaximum(true);
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
                ((JInternalFrame) getFrame()).setMaximum(true);
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
