/*
 * Copyright (c) 2006-2013 DMDirc Developers
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
import com.dmdirc.interfaces.FrameComponentChangeListener;
import com.dmdirc.interfaces.FrameInfoListener;
import com.dmdirc.interfaces.NotificationListener;
import com.dmdirc.ui.Colour;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.messages.Formatter;
import com.dmdirc.ui.messages.IRCDocument;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.util.URLBuilder;
import com.dmdirc.util.collections.ListenerList;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.Getter;
import lombok.ListenerSupport;

/**
 * The frame container implements basic methods that should be present in
 * all objects that handle a frame.
 */
@ListenerSupport({NotificationListener.class, FrameInfoListener.class})
@SuppressWarnings("PMD.UnusedPrivateField")
public abstract class FrameContainer {

    /** Listeners not yet using ListenerSupport. */
    protected final ListenerList listeners = new ListenerList();

    /** The colour of our frame's notifications. */
    @Getter
    private Colour notification = Colour.BLACK;

    /** The document used to store this container's content. */
    private IRCDocument document;

    /** The children of this frame. */
    private final Collection<FrameContainer> children
            = new CopyOnWriteArrayList<>();

    /** The parent of this frame. */
    @Getter
    private FrameContainer parent;

    /** The name of the icon being used for this container's frame. */
    @Getter
    private String icon;

    /** The name of this container. */
    @Getter
    private String name;

    /** The title of this container. */
    @Getter
    private String title;

    /** The config manager for this container. */
    @Getter
    private final ConfigManager configManager;

    /** The IconChanger for this container. */
    private final IconChanger changer = new IconChanger();

    /** The UI components that this frame requires. */
    private final Set<String> components;

    /** The styliser used by this container. */
    private Styliser styliser;

    /** Object used to synchronise styliser access. */
    private final Object styliserSync = new Object();

    /** Object used to synchronise styliser access. */
    private final Object documentSync = new Object();

    /** The IconManager for this container. */
    @Getter
    private final IconManager iconManager;

    /**
     * Instantiate new frame container.
     *
     * @param icon The icon to use for this container
     * @param name The name of this container
     * @param title The title of this container
     * @param config The config manager for this container
     * @param components The UI components that this frame requires
     * @since 0.6.4
     */
    protected FrameContainer(
            final String icon, final String name,
            final String title, final ConfigManager config,
            final Collection<String> components) {
        this.configManager = config;
        this.name = name;
        this.title = title;
        this.components = new HashSet<>(components);

        iconManager = new IconManager(config, URLBuilder.getInstance());

        setIcon(icon);
    }

    /**
     * Returns a collection of direct children of this frame.
     *
     * @return This frame's children
     * @since 0.6.4
     */
    public Collection<FrameContainer> getChildren() {
        return Collections.unmodifiableCollection(children);
    }

    /**
     * Determines whether the specified target is a child of this container.
     * Children may be indirect (i.e., a child of another child).
     *
     * @param target The window to be tested
     * @return True if the specified container is a child of this one
     */
    public boolean isChild(final FrameContainer target) {
        if (children.contains(target)) {
            return true;
        }

        for (FrameContainer child : children) {
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
    public void addChild(final FrameContainer child) {
        children.add(child);
        child.setParent(this);
    }

    /**
     * Removes a child window from this frame.
     *
     * @param child The window to be removed
     * @since 0.6.4
     */
    public void removeChild(final FrameContainer child) {
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
    public synchronized void setParent(final FrameContainer parent) {
        if (this.parent != null && (parent == null || !parent.equals(this.parent))) {
            this.parent.removeChild(this);
        }

        this.parent = parent;
    }

    /**
     * Retrieves the {@link IRCDocument} used to store this frame's content.
     *
     * @return This frame's document
     * @since 0.6.4
     */
    public IRCDocument getDocument() {
        synchronized (documentSync) {
            if (document == null) {
                document = new IRCDocument(getConfigManager(), getStyliser());
            }
            return document;
        }
    }

    /**
     * Changes the name of this container, and notifies any
     * {@link FrameInfoListener}s of the change.
     *
     * @param name The new name for this frame.
     */
    protected void setName(final String name) {
        this.name = name;

        fireNameChanged(this, name);
    }

    /**
     * Changes the title of this container, and notifies any
     * {@link FrameInfoListener}s of the change.
     *
     * @param title The new title for this frame.
     */
    public void setTitle(final String title) {
        this.title = title;

        fireTitleChanged(this, title);
    }

    /**
     * Returns the collection of UI component identifiers that this frame
     * container requires for its display.
     *
     * @since 0.6.6
     * @return Collection of UI component identifiers
     */
    public Set<String> getComponents() {
        return Collections.unmodifiableSet(components);
    }

    /**
     * Adds a new component to this container.
     *
     * @since 0.6.6
     * @param component The component to be added
     */
    public void addComponent(final String component) {
        components.add(component);

        for (FrameComponentChangeListener listener
                : listeners.get(FrameComponentChangeListener.class)) {
            listener.componentAdded(this, component);
        }
    }

    /**
     * Removes a component from this container.
     *
     * @since 0.6.6
     * @param component The component to be removed
     */
    public void removeComponent(final String component) {
        components.remove(component);

        for (FrameComponentChangeListener listener
                : listeners.get(FrameComponentChangeListener.class)) {
            listener.componentRemoved(this, component);
        }
    }

    /**
     * Closes this container (and it's associated frame).
     */
    public void close() {
        for (FrameCloseListener listener : listeners.get(FrameCloseListener.class)) {
            listener.windowClosing(this);
        }

        windowClosing();

        WindowManager.getWindowManager().removeWindow(this);
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

        configManager.removeListener(changer);
        configManager.addChangeListener("icon", icon, changer);
    }

    /**
     * Called when this container's icon is updated.
     */
    private void iconUpdated() {
        fireIconChanged(this, icon);
    }

    /**
     * Retrieves the styliser which should be used by this container.
     *
     * @return this container's styliser
     */
    public Styliser getStyliser() {
        synchronized (styliserSync) {
            if (styliser == null) {
                styliser = new Styliser(getServer(), getConfigManager());
            }
            return styliser;
        }
    }

    /**
     * Clears any outstanding notifications this frame has set.
     */
    public void clearNotification() {
        // TODO: This should default ot something colour independent
        notification = Colour.BLACK;

        fireNotificationCleared(this);
    }

    /**
     * Sends a notification to the frame manager if this fame isn't active.
     *
     * @param colour The colour to use for the notification
     */
    public void sendNotification(final Colour colour) {
        if (!colour.equals(notification)) {
            notification = colour;

            fireNotificationSet(this, colour);
        }
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
     * Invoked when our window has been closed.
     * <p>
     * At this point, all interested parties have been told that the window
     * has been closed, and therefore any references to frames or other
     * resources may be completely freed.
     */
    public abstract void windowClosed();

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
        final List<String[]> lines = new LinkedList<>();
        for (final String myLine : line.split("\n")) {
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

            ActionManager.getActionManager().triggerEvent(
                    CoreActionType.CLIENT_LINE_ADDED, null, this, myLine);
        }

        getDocument().addText(lines);
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
     * Adds a component listener to this container.
     *
     * @since 0.6.6
     * @param listener The listener to be added
     */
    public void addComponentListener(final FrameComponentChangeListener listener) {
        listeners.add(FrameComponentChangeListener.class, listener);
    }

    /**
     * Removes a component listener from this container.
     *
     * @since 0.6.6
     * @param listener The listener to be removed
     */
    public void removeComponentListener(final FrameComponentChangeListener listener) {
        listeners.remove(FrameComponentChangeListener.class, listener);
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
