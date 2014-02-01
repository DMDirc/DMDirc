/*
 * Copyright (c) 2006-2014 DMDirc Developers
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
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.FrameCloseListener;
import com.dmdirc.interfaces.FrameComponentChangeListener;
import com.dmdirc.interfaces.FrameInfoListener;
import com.dmdirc.interfaces.NotificationListener;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.ui.Colour;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.messages.Formatter;
import com.dmdirc.ui.messages.IRCDocument;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.util.URLBuilder;
import com.dmdirc.util.collections.ListenerList;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The frame container implements basic methods that should be present in
 * all objects that handle a frame.
 */
public abstract class FrameContainer {

    /** Listeners not yet using ListenerSupport. */
    protected final ListenerList listeners = new ListenerList();

    /** The colour of our frame's notifications. */
    private Colour notification = Colour.BLACK;

    /** The document used to store this container's content. */
    private IRCDocument document;

    /** The children of this frame. */
    private final Collection<FrameContainer> children
            = new CopyOnWriteArrayList<>();

    /** The parent of this frame. */
    private FrameContainer parent;

    /** The name of the icon being used for this container's frame. */
    private String icon;

    /** The name of this container. */
    private String name;

    /** The title of this container. */
    private String title;

    /** The config manager for this container. */
    private final AggregateConfigProvider configManager;

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

    /** Previously-constructed iconmanagers. */
    private final Map<URLBuilder, IconManager> iconManagers = new HashMap<>();

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
            final String title, final AggregateConfigProvider config,
            final Collection<String> components) {
        this.configManager = config;
        this.name = name;
        this.title = title;
        this.components = new HashSet<>(components);

        setIcon(icon);
    }

    public Colour getNotification() {
        return notification;
    }

    public FrameContainer getParent() {
        return parent;
    }

    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public AggregateConfigProvider getConfigManager() {
        return configManager;
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
     * Gets an icon manager for this container.
     *
     * @return An icon manager for this container.
     * @deprecated Use {@link #getIconManager(com.dmdirc.util.URLBuilder)}.
     */
    @Deprecated
    public IconManager getIconManager() {
        return getIconManager(URLBuilder.getInstance());
    }

    /**
     * Gets an icon manager for this container.
     *
     * @param urlBuilder The builder to use to construct icon URLs.
     * @return An icon manager for this container.
     */
    public IconManager getIconManager(final URLBuilder urlBuilder) {
        if (!iconManagers.containsKey(urlBuilder)) {
            iconManagers.put(urlBuilder, new IconManager(getConfigManager(), urlBuilder));
        }
        return iconManagers.get(urlBuilder);
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

        listeners.getCallable(FrameInfoListener.class).nameChanged(this, name);
    }

    /**
     * Changes the title of this container, and notifies any
     * {@link FrameInfoListener}s of the change.
     *
     * @param title The new title for this frame.
     */
    public void setTitle(final String title) {
        this.title = title;

        listeners.getCallable(FrameInfoListener.class).titleChanged(this, title);
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
    }

    /**
     * Returns the connection that this container is associated with.
     *
     * @return the associated connection, or {@code null}.
     */
    public abstract Connection getConnection();

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
        listeners.getCallable(FrameInfoListener.class).iconChanged(this, icon);
    }

    /**
     * Retrieves the styliser which should be used by this container.
     *
     * @return this container's styliser
     */
    public Styliser getStyliser() {
        synchronized (styliserSync) {
            if (styliser == null) {
                styliser = new Styliser(getConnection(), getConfigManager());
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

        listeners.getCallable(NotificationListener.class).notificationCleared(this);
    }

    /**
     * Sends a notification to the frame manager if this fame isn't active.
     *
     * @param colour The colour to use for the notification
     */
    public void sendNotification(final Colour colour) {
        if (!colour.equals(notification)) {
            notification = colour;

            listeners.getCallable(NotificationListener.class).notificationSet(this, colour);
        }
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
     * Adds a notification listener to this container.
     *
     * @param listener The listener to inform of notification events.
     */
    public void addNotificationListener(final NotificationListener listener) {
        listeners.add(NotificationListener.class, listener);
    }

    /**
     * Removes a notification listener from this container.
     *
     * @param listener The listener to be removed.
     */
    public void removeNotificationListener(final NotificationListener listener) {
        listeners.remove(NotificationListener.class, listener);
    }

    /**
     * Adds a frame info listener to this container.
     *
     * @param listener The listener to be informed of frame information changes.
     */
    public void addFrameInfoListener(final FrameInfoListener listener) {
        listeners.add(FrameInfoListener.class, listener);
    }

    /**
     * Removes a frame info listener from this container.
     *
     * @param listener The listener to be removed.
     */
    public void removeFrameInfoListener(final FrameInfoListener listener) {
        listeners.remove(FrameInfoListener.class, listener);
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
