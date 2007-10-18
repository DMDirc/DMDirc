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

import com.dmdirc.config.ConfigManager;
import com.dmdirc.interfaces.NotificationListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.interfaces.Window;

import java.awt.Color;
import java.beans.PropertyVetoException;

import javax.swing.Icon;
import javax.swing.event.EventListenerList;

/**
 * The frame container implements basic methods that should be present in
 * all objects that handle a frame.
 *
 * @author chris
 */
public abstract class FrameContainer {

    /** The colour of our frame's notifications. */
    protected Color notification = Color.BLACK;

    /** A list of listeners for this containers's events. */
    protected final EventListenerList listeners = new EventListenerList();

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
        if (getServer() == null) {
            return null;
        } else {
            return getServer().getConfigManager();
        }
    }

    /**
     * Requests that this object's frame be activated.
     */
    public void activateFrame() {
        Main.getUI().getMainWindow().setActiveFrame(getFrame());
    }

    /**
     * Clears any outstanding notifications this frame has set.
     */
    protected void clearNotification() {
        // TODO: This should default ot something colour independent
        notification = Color.BLACK;

        final Object[] listenerList = listeners.getListenerList();
        for (int i = 0; i < listenerList.length; i += 2) {
            if (listenerList[i] == NotificationListener.class) {
                ((NotificationListener) listenerList[i + 1])
                        .notificationCleared(getFrame());
             }
        }
    }

    /**
     * Sends a notification to the frame manager if this fame isn't active.
     *
     * @param colour The colour to use for the notification
     */
    public void sendNotification(final Color colour) {
        final Window activeFrame = Main.getUI().getMainWindow().getActiveFrame();
        if (activeFrame != null && !activeFrame.equals(getFrame())
                && !colour.equals(notification)) {
            notification = colour;

            final Object[] listenerList = listeners.getListenerList();
            for (int i = 0; i < listenerList.length; i += 2) {
                if (listenerList[i] == NotificationListener.class) {
                    ((NotificationListener) listenerList[i + 1])
                            .notificationSet(getFrame(), colour);
                }
            }
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
        if (getServer() == null || getConfigManager() == null || getFrame() == null) {
            return;
        }

        final boolean pref = getConfigManager().getOptionBool("ui", "maximisewindows", false);
        if (pref || Main.getUI().getMainWindow().getMaximised()) {
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
        if (getFrame() == null) {
            return;
        }

        if (Main.getUI().getMainWindow().getMaximised()) {
            try {
                getFrame().setMaximum(true);
            } catch (PropertyVetoException ex) {
                Logger.userError(ErrorLevel.LOW, "Unable to maximise window");
            }
        }
        Main.getUI().getMainWindow().getFrameManager().setSelected(this);
        clearNotification();

        if (getServer() != null) {
            getServer().setActiveFrame(this);
        }
    }

    /**
     * Invoked when our window is deactivated.
     */
    public void windowDeactivated() {
        // Do nothing.
    }

    /**
     * Adds a line to this container's window. If the window is null for some
     * reason, the line is silently discarded.
     *
     * @param type The message type to use
     * @param args The message's arguments
     */
    protected void addLine(final String type, final Object ... args) {
        if (getFrame() != null) {
            getFrame().addLine(type, args);
        }
    }

    /**
     * Adds a line to this container's window. If the window is null for some
     * reason, the line is silently discarded.
     *
     * @param type The message type to use
     * @param args The message's arguments
     */
    protected void addLine(final StringBuffer type, final Object ... args) {
        if (getFrame() != null) {
            getFrame().addLine(type, args);
        }
    }

    /**
     * Adds a notification listener for this frame container.
     *
     * @param listener The listener to be added
     */
    public void addNotificationListener(final NotificationListener listener) {
        listeners.add(NotificationListener.class, listener);
    }

    /**
     * Removes a notification listener from this frame container.
     *
     * @param listener The listener to be removed
     */
    public void removeNotificationListener(final NotificationListener listener) {
        listeners.remove(NotificationListener.class, listener);
    }
}
