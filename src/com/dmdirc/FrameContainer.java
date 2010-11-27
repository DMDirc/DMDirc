/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.interfaces.FrameCloseListener;
import com.dmdirc.interfaces.FrameInfoListener;
import com.dmdirc.interfaces.NotificationListener;
import com.dmdirc.interfaces.SelectionListener;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.messages.Formatter;
import com.dmdirc.ui.messages.IRCDocument;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.util.ListenerList;
import com.dmdirc.util.StringTranscoder;

import java.awt.Color;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The frame container implements basic methods that should be present in
 * all objects that handle a frame.
 *
 * @param <T> The type of window which should be used for this frame container.
 * @author chris
 */
public abstract class FrameContainer<T extends Window> {

    /** The colour of our frame's notifications. */
    protected Color notification = Color.BLACK;

    /** A list of listeners for this containers's events. */
    protected final ListenerList listeners = new ListenerList();

    /** The document used to store this container's content. */
    protected final IRCDocument document;

    /** The children of this frame. */
    protected final Collection<FrameContainer<?>> children
            = new CopyOnWriteArrayList<FrameContainer<?>>();

    /** The windows representing of this frame. */
    protected final Collection<T> windows
            = new CopyOnWriteArrayList<T>();

    /** The class of windows we want to represent this container. */
    protected final Class<T> windowClass;

    /** The parent of this frame. */
    protected FrameContainer<?> parent;

    /** The name of the icon being used for this container's frame. */
    private String icon;

    /** The name of this container. */
    private String name;

    /** The title of this container. */
    private String title;

    /** The transcoder to use for this container. */
    private final StringTranscoder transcoder;

    /** The config manager for this container. */
    private final ConfigManager config;

    /** The IconChanger for this container. */
    private final IconChanger changer = new IconChanger();

    /** The styliser used by this container. */
    private final Styliser styliser;

    /**
     * Instantiate new frame container.
     *
     * @param icon The icon to use for this container
     * @param name The name of this container
     * @param title The title of this container
     * @param windowClass The class of windows required to represent this container
     * @param config The config manager for this container
     * @since 0.6.4
     */
    protected FrameContainer(final String icon, final String name,
            final String title, final Class<T> windowClass,
            final ConfigManager config) {
        this.config = config;
        this.name = name;
        this.title = title;
        this.windowClass = windowClass;
        this.styliser = new Styliser(this);
        this.document = new IRCDocument(getConfigManager(), getStyliser());

        // Can't assign directly to transcoder as it's final, and Java doesn't
        // like the two paths in the try/catch.
        StringTranscoder tempTranscoder;
        try {
            tempTranscoder = new StringTranscoder(Charset.forName(
                    config.getOption("channel", "encoding")));
        } catch (IllegalArgumentException ex) {
            tempTranscoder = new StringTranscoder(Charset.forName("UTF-8"));
        }
        transcoder = tempTranscoder;

        setIcon(icon);
    }

    /**
     * Returns the internal frame associated with this object.
     *
     * @return The internal frame associated with this object
     * @deprecated Use {@link #getWindows()} instead
     */
    @Deprecated
    public final T getFrame() {
        return getWindows().isEmpty() ? null : getWindows().iterator().next();
    }

    /**
     * Returns a collection of direct children of this frame.
     *
     * @return This frame's children
     * @since 0.6.4
     */
    public Collection<FrameContainer<?>> getChildren() {
        return Collections.unmodifiableCollection(children);
    }

    /**
     * Determines whether the specified target is a child of this container.
     * Children may be indirect (i.e., a child of another child).
     *
     * @param target The window to be tested
     * @return True if the specified container is a child of this one
     */
    public boolean isChild(final FrameContainer<?> target) {
        if (children.contains(target)) {
            return true;
        }

        for (FrameContainer<?> child : children) {
            if (child.isChild(target)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds a new child window to this frame.
     *
     * @param child The window to be added
     * @since 0.6.4
     */
    public void addChild(final FrameContainer<?> child) {
        children.add(child);
        child.setParent(this);
    }

    /**
     * Removes a child window from this frame.
     *
     * @param child The window to be removed
     * @since 0.6.4
     */
    public void removeChild(final FrameContainer<?> child) {
        children.remove(child);
    }

    /**
     * Sets the parent of this container to the one specified. If this
     * container already had a parent, it will deregister itself with the
     * old parent.
     *
     * @param parent The new parent for this container
     * @since 0.6.4
     */
    public synchronized void setParent(final FrameContainer<?> parent) {
        if (this.parent != null && !parent.equals(this.parent)) {
            this.parent.removeChild(this);
        }

        this.parent = parent;
    }

    /**
     * Retrieves the parent of this container, if there is one.
     *
     * @return This container's parent, or null if it is a top level window.
     * @since 0.6.4
     */
    public FrameContainer<?> getParent() {
        return parent;
    }

    /**
     * Retrieves the {@link IRCDocument} used to store this frame's content.
     *
     * @return This frame's document
     * @since 0.6.4
     */
    public IRCDocument getDocument() {
        return document;
    }

    /**
     * Retrieves the {@link StringTranscoder} used to transcode this frame's
     * text.
     *
     * @return This frame's transcoder
     * @since 0.6.4
     */
    public StringTranscoder getTranscoder() {
        return transcoder;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Retrieves the name of this container.
     *
     * @return This container's name
     * @since 0.6.3m2
     */
    public String getName() {
        return name;
    }

    /**
     * Changes the name of this container, and notifies any
     * {@link FrameInfoListener}s of the change.
     *
     * @param name The new name for this frame.
     */
    protected void setName(final String name) {
        this.name = name;

        for (FrameInfoListener listener : listeners.get(
                FrameInfoListener.class)) {
            listener.nameChanged(this, name);
        }
    }

    /**
     * Retrieves the title which should be used by this container's windows.
     *
     * @return This container's title
     * @since 0.6.4
     */
    public String getTitle() {
        return title;
    }

    /**
     * Changes the title of this container, and notifies any
     * {@link FrameInfoListener}s of the change.
     *
     * @param title The new title for this frame.
     */
    public void setTitle(final String title) {
        this.title = title;

        for (FrameInfoListener listener : listeners.get(
                FrameInfoListener.class)) {
            listener.titleChanged(this, title);
        }
    }

    /**
     * Closes this container (and it's associated frame).
     */
    public void close() {
        for (Window window : getWindows()) {
            window.close();
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
        for (FrameInfoListener listener : listeners.get(
                FrameInfoListener.class)) {
            listener.iconChanged(this, icon);
        }
    }

    /**
     * Retrieves the name of the icon used by this container's window.
     *
     * @return This container's icon
     */
    public final String getIcon() {
        return icon;
    }

    /**
     * Returns the config manager for this container.
     *
     * @return the associated config manager
     */
    public ConfigManager getConfigManager() {
        return config;
    }

    /**
     * Retrieves the styliser which should be used by this container.
     *
     * @return this container's styliser
     */
    public Styliser getStyliser() {
        return styliser;
    }

    /**
     * Requests that this object's frame be activated.
     */
    public void activateFrame() {
        for (Window window : getWindows()) {
            window.activateFrame();
        }
    }

    /**
     * Clears any outstanding notifications this frame has set.
     */
    protected void clearNotification() {
        // TODO: This should default ot something colour independent
        notification = Color.BLACK;

        for (NotificationListener listener : listeners.get(
                NotificationListener.class)) {
            listener.notificationCleared(this);
        }
    }

    /**
     * Sends a notification to the frame manager if this fame isn't active.
     *
     * @param colour The colour to use for the notification
     */
    public void sendNotification(final Color colour) {
        final FrameContainer<?> activeWindow = WindowManager.getActiveWindow();

        if (activeWindow != null && !activeWindow.equals(this)
                && !colour.equals(notification)) {
            notification = colour;

            for (NotificationListener listener : listeners.get(
                    NotificationListener.class)) {
                listener.notificationSet(this, colour);
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
    @SuppressWarnings("element-type-mismatch")
    public boolean ownsFrame(final Window target) {
        return windows.contains(target);
    }

    /**
     * Invoked when our window has been opened.
     * @deprecated Pointless. Stop calling me.
     */
    @Deprecated
    public void windowOpened() {
    }

    /**
     * Invoked when our window is closing.
     * <p>
     * Frame containers must perform the following actions in this order:
     * <ol>
     *  <li>Make the window non-visible (so it appears 'closed' to the user)</li>
     *  <li>Remove any callbacks or listeners (events should not be processed
     *      once a window has been requested to close)</li>
     *  <li>Trigger any actions necessary (terminating any TCP connections,
     *      disconnecting parsers, closing children, etc)</li>
     *  <li>Trigger action for the window closing (raise a DMDirc action for
     *      the closure of the window, if required)</li>
     *  <li>Inform any parents that the window is closing (this includes
     *      unregistering the window with any specific managers, or from the
     *      parent windows if they track children)</li>
     *  <li>Remove the window from the window manager (by calling
     *      {@link WindowManager#removeWindow(com.dmdirc.ui.interfaces.Window)}</li>
     * </ol>
     * <p>
     * <strong>NB:</strong> As of DMDirc 0.6.5, points 1 and 6 (making windows
     * non-visible and removing the window from the window manager) are handled
     * by the caller of this method, and should <strong>not</strong> be
     * implemented by subclasses.
     * </p>
     * <p>
     * While resources may be relinquished in step three, references MUST NOT
     * be removed yet. That is, if a window holds a resource, the resource may
     * be closed, but the relevant object MUST still be available for
     * interrogation at the end of this method.
     * <p>
     * This behaviour is required so that parties receiving windowDeleted events
     * from the WindowManager may inspect the closing window and perform actions
     * on its frame, parser, etc. The resources should be completely freed in
     * the {@link #windowClosed()} method.
     */
    protected abstract void windowClosing();

    /**
     * Handles the closing of this container. This should be called by UI
     * components when the user is attempting to close a corresponding window.
     */
    public void handleWindowClosing() {
        for (FrameCloseListener listener : listeners.get(FrameCloseListener.class)) {
            listener.windowClosing(this);
        }

        windowClosing();

        WindowManager.removeWindow(this);
    }

    /**
     * Invoked when our window has been closed.
     * <p>
     * At this point, all interested parties have been told that the window
     * has been closed, and therefore any references to frames or other
     * resources may be completely freed.
     */
    public abstract void windowClosed();

    /**
     * Invoked when our window is activated.
     */
    public void windowActivated() {
        for (SelectionListener listener : listeners.get(
                SelectionListener.class)) {
            listener.selectionChanged(this);
        }

        clearNotification();

        if (getServer() != null) {
            getServer().setActiveFrame(this);
        }
    }

    /**
     * Invoked when our window is deactivated.
     * @deprecated Not used. Stop calling me.
     */
    @Deprecated
    public void windowDeactivated() {
    }

    /**
     * Adds a line to this container's window. If the window is null for some
     * reason, the line is silently discarded.
     *
     * @param type The message type to use
     * @param timestamp The timestamp to use for this line
     * @param args The message's arguments
     * @since 0.6.4
     */
    public void addLine(final String type, final Date timestamp,
            final Object ... args) {
        if (type != null && !type.isEmpty()) {
            addLine(Formatter.formatMessage(getConfigManager(), type, args),
                    timestamp);
        }
    }

    /**
     * Adds a line to this container's window. If the window is null for some
     * reason, the line is silently discarded.
     *
     * @param type The message type to use
     * @param args The message's arguments
     */
    public void addLine(final String type, final Object ... args) {
        addLine(type, new Date(), args);
    }

    /**
     * Adds a line to this container's window. If the window is null for some
     * reason, the line is silently discarded.
     *
     * @param type The message type to use
     * @param timestamp The timestamp to use for this line
     * @param args The message's arguments
     * @since 0.6.4
     */
    public void addLine(final StringBuffer type, final Date timestamp,
            final Object ... args) {
        if (type != null) {
            addLine(type.toString(), timestamp, args);
        }
    }

    /**
     * Adds a line to this container's window. If the window is null for some
     * reason, the line is silently discarded.
     *
     * @param type The message type to use
     * @param args The message's arguments
     */
    public void addLine(final StringBuffer type, final Object ... args) {
        addLine(type, new Date(), args);
    }

    /**
     * Adds the specified raw line to the window, without using a formatter.
     *
     * @param line The line to be added
     * @param timestamp Whether or not to display the timestamp for this line
     */
    public void addLine(final String line, final boolean timestamp) {
        addLine(line, timestamp ? new Date() : null);
    }

    /**
     * Adds the specified raw line to the window, without using a formatter,
     * and using the specified timestamp. If the timestamp is <code>null</code>,
     * no timestamp is added.
     *
     * @param line The line to be added
     * @param timestamp The timestamp to use for the line
     * @since 0.6.4
     */
    public void addLine(final String line, final Date timestamp) {
        final String encodedLine = transcoder.decode(line);
        final List<String[]> lines = new LinkedList<String[]>();
        for (final String myLine : encodedLine.split("\n")) {
            if (timestamp != null) {
                lines.add(new String[]{
                    Formatter.formatMessage(getConfigManager(), "timestamp",
                            timestamp),
                    myLine,
                });
            } else {
                lines.add(new String[]{
                    myLine,
                });
            }

            ActionManager.processEvent(CoreActionType.CLIENT_LINE_ADDED,
                    null, this, myLine);
        }

        document.addText(lines);
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

    /**
     * Adds a close listener for this frame container.
     *
     * @since 0.6.5
     * @param listener The listener to be added
     */
    public void addCloseListener(final FrameCloseListener listener) {
        listeners.add(FrameCloseListener.class, listener);
    }

    /**
     * Removes a close listener from this frame container.
     *
     * @since 0.6.5
     * @param listener The listener to be removed
     */
    public void removeCloseListener(final FrameCloseListener listener) {
        listeners.remove(FrameCloseListener.class, listener);
    }

    /**
     * Adds a selection listener for this frame container.
     *
     * @param listener The listener to be added
     */
    public void addSelectionListener(final SelectionListener listener) {
        listeners.add(SelectionListener.class, listener);
    }

    /**
     * Removes a selection listener from this frame container.
     *
     * @param listener The listener to be removed
     */
    public void removeSelectionListener(final SelectionListener listener) {
        listeners.remove(SelectionListener.class, listener);
    }

    /**
     * Adds a frame info listener for this frame container.
     *
     * @param listener The listener to be added
     */
    public void addFrameInfoListener(final FrameInfoListener listener) {
        listeners.add(FrameInfoListener.class, listener);
    }

    /**
     * Removes a frame info listener from this frame container.
     *
     * @param listener The listener to be removed
     */
    public void removeFrameInfoListener(final FrameInfoListener listener) {
        listeners.remove(FrameInfoListener.class, listener);
    }

    /**
     * Adds a new window to this container.
     *
     * @param window The window to be added
     * @since 0.6.4
     */
    public void addWindow(final T window) {
        windows.add(window);
    }

    /**
     * Removes the specified window from this container.
     *
     * @param window The window to be removed
     * @since 0.6.4
     */
    public void removeWindow(final T window) {
        windows.remove(window);
    }

    /**
     * Retrieves a collection of windows that represent this container.
     *
     * @return The collection of windows currently representing this container
     * @since 0.6.4
     */
    public Collection<T> getWindows() {
        return windows;
    }

    /**
     * Retrieves the class of windows which should be used to represent this
     * container.
     *
     * @return This container's window class
     * @since 0.6.4
     */
    public Class<T> getWindowClass() {
        return windowClass;
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
