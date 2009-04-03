/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.interfaces.IconChangeListener;
import com.dmdirc.interfaces.NotificationListener;
import com.dmdirc.interfaces.SelectionListener;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.util.ListenerList;

import java.awt.Color;

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

    /** A list of listeners for this containers's events. */
    protected final ListenerList listeners = new ListenerList();

    /** The name of the icon being used for this container's frame. */
    private String icon;
    
    /** The config manager for this container. */
    private final ConfigManager config;
    
    /** The IconChanger for this container. */
    private final IconChanger changer = new IconChanger();

    /**
     * Instantiate new frame container.
     * 
     * @param icon The icon to use for this container
     * @param config The config manager for this container
     */
    public FrameContainer(final String icon, final ConfigManager config) {
        this.config = config;
        
        setIcon(icon);
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
    @Override
    public abstract String toString();

    /**
     * Closes this container (and it's associated frame).
     */
    public void close() {
        if (getFrame() == null) {
            throw new IllegalStateException("No frame associated with this container!");
        } else {
            getFrame().close();
        }
    }

    /**
     * Returns the server instance associated with this container.
     *
     * @return the associated server connection
     */
    public abstract Server getServer();

    /**
     * Sets the icon to be used by this frame container.
     * 
     * @param icon The new icon to be used
     */
    public final void setIcon(final String icon) {
        this.icon = icon;
        
        iconUpdated();
        
        config.removeListener(changer);
        config.addChangeListener("icon", icon, changer);
    }
    
    /**
     * Called when this container's icon is updated.
     */
    private void iconUpdated() {
        final Icon newIcon = getIcon();
        
        synchronized (listeners) {
            for (IconChangeListener listener : listeners.get(IconChangeListener.class)) {
                listener.iconChanged(getFrame(), newIcon);
            }
        }
    }

    /**
     * Retrieves the icon used by this container's window.
     *
     * @return This container's icon
     */
    public final Icon getIcon() {
        return IconManager.getIconManager().getIcon(icon);
    }

    /**
     * Returns the config manager for this container.
     *
     * @return the associated config manager
     */
    public final ConfigManager getConfigManager() {
        return config;
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

        synchronized (listeners) {
            for (NotificationListener listener : listeners.get(NotificationListener.class)) {
                listener.notificationCleared(getFrame());
            }
        }
    }

    /**
     * Sends a notification to the frame manager if this fame isn't active.
     *
     * @param colour The colour to use for the notification
     */
    public void sendNotification(final Color colour) {
        final Window activeFrame = Main.getUI().getActiveWindow();

        if (activeFrame != null && !activeFrame.equals(getFrame())
                && !colour.equals(notification)) {
            notification = colour;

            synchronized (listeners) {
                for (NotificationListener listener : listeners.get(NotificationListener.class)) {
                    listener.notificationSet(getFrame(), colour);
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
        if (config == null || getFrame() == null) {
            return;
        }

        final boolean pref = getConfigManager().getOptionBool("ui", "maximisewindows");
        if (pref || Main.getUI().getMainWindow().getMaximised()) {
            getFrame().maximise();
        }
    }

    /**
     * Invoked when our window is closing.
     */
    public abstract void windowClosing();

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
            getFrame().maximise();
        }

        synchronized (listeners) {
            for (SelectionListener listener : listeners.get(SelectionListener.class)) {
                listener.selectionChanged(getFrame());
            }
        }

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
        synchronized (listeners) {
            listeners.add(NotificationListener.class, listener);
        }
    }

    /**
     * Removes a notification listener from this frame container.
     *
     * @param listener The listener to be removed
     */
    public void removeNotificationListener(final NotificationListener listener) {
        synchronized (listeners) {
            listeners.remove(NotificationListener.class, listener);
        }
    }

    /**
     * Adds a selection listener for this frame container.
     *
     * @param listener The listener to be added
     */
    public void addSelectionListener(final SelectionListener listener) {
        synchronized (listeners) {
            listeners.add(SelectionListener.class, listener);
        }
    }

    /**
     * Removes a selection listener from this frame container.
     *
     * @param listener The listener to be removed
     */
    public void removeSelectionListener(final SelectionListener listener) {
        synchronized (listeners) {
            listeners.remove(SelectionListener.class, listener);
        }
    }

    /**
     * Adds an icon change listener for this frame container.
     *
     * @param listener The listener to be added
     */
    public void addIconChangeListener(final IconChangeListener listener) {
        synchronized (listeners) {
            listeners.add(IconChangeListener.class, listener);
        }
    }

    /**
     * Removes an icon change listener from this frame container.
     *
     * @param listener The listener to be removed
     */
    public void removeIconChangeListener(final IconChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(IconChangeListener.class, listener);
        }
    }
    
    /**
     * Updates the icon of this frame if its config setting is changed.
     */
    private class IconChanger implements ConfigChangeListener {

        /** {@inheritDoc} */
        @Override
        public void configChanged(final String domain, final String key) {
            iconUpdated();
        }
        
    }
}
