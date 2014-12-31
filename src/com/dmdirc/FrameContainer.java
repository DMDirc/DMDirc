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

import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.events.ClientLineAddedEvent;
import com.dmdirc.events.DisplayPropertyMap;
import com.dmdirc.events.FrameClosingEvent;
import com.dmdirc.events.FrameComponentAddedEvent;
import com.dmdirc.events.FrameComponentRemovedEvent;
import com.dmdirc.events.FrameIconChangedEvent;
import com.dmdirc.events.FrameNameChangedEvent;
import com.dmdirc.events.FrameTitleChangedEvent;
import com.dmdirc.events.NotificationClearedEvent;
import com.dmdirc.events.NotificationSetEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.parser.common.CompositionState;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.messages.BackBuffer;
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.ui.messages.Formatter;
import com.dmdirc.ui.messages.IRCDocument;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.ui.messages.UnreadStatusManager;
import com.dmdirc.ui.messages.sink.MessageSinkManager;
import com.dmdirc.util.ChildEventBusManager;
import com.dmdirc.util.URLBuilder;
import com.dmdirc.util.collections.ListenerList;
import com.dmdirc.util.colours.Colour;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkState;

/**
 * The frame container implements basic methods that should be present in all objects that handle a
 * frame.
 */
public abstract class FrameContainer {

    /** Listeners not yet using ListenerSupport. */
    protected final ListenerList listeners = new ListenerList();
    /** The colour of our frame's notifications. */
    private Optional<Colour> notification = Optional.empty();
    /** The children of this frame. */
    private final Collection<FrameContainer> children = new CopyOnWriteArrayList<>();
    /** The parent of this frame. */
    private final Optional<FrameContainer> parent;
    /** The name of the icon being used for this container's frame. */
    private String icon;
    /** The name of this container. */
    private String name;
    /** The title of this container. */
    private String title;
    /** The config manager for this container. */
    private final AggregateConfigProvider configManager;
    /** The IconChanger for this container. */
    private final ConfigChangeListener changer = (d, k) -> iconUpdated();
    /** The UI components that this frame requires. */
    private final Set<String> components;
    /** The manager to use to manage our event bus. */
    private final ChildEventBusManager eventBusManager;
    /** Event bus to dispatch events to. */
    private final DMDircMBassador eventBus;
    /** The icon manager to use for this container. */
    private final IconManager iconManager;
    /** The manager handling this frame's unread status. */
    private final UnreadStatusManager unreadStatusManager;
    /** Whether or not this container is writable. */
    private final boolean writable;
    /** The back buffer factory. */
    private final BackBufferFactory backBufferFactory;
    /** The back buffer for this container. */
    private BackBuffer backBuffer;
    /** Lock for access to {@link #backBuffer}. */
    private final Object backBufferLock = new Object();

    /**
     * The command parser used for commands in this container.
     * <p>
     * Only defined if this container is {@link #writable}.
     */
    private final Optional<CommandParser> commandParser;
    /**
     * The manager to use to dispatch messages to sinks.
     * <p>
     * Only defined if this container is {@link #writable}.
     */
    private final Optional<MessageSinkManager> messageSinkManager;
    /**
     * The tab completer to use.
     * <p>
     * Only defined if this container is {@link #writable}.
     */
    private final Optional<TabCompleter> tabCompleter;

    /**
     * Instantiate new frame container.
     */
    protected FrameContainer(
            @Nullable final FrameContainer parent,
            final String icon,
            final String name,
            final String title,
            final AggregateConfigProvider config,
            final BackBufferFactory backBufferFactory,
            final URLBuilder urlBuilder,
            final DMDircMBassador eventBus,
            final Collection<String> components) {
        this.parent = Optional.ofNullable(parent);
        this.configManager = config;
        this.name = name;
        this.title = title;
        this.components = new HashSet<>(components);
        this.iconManager = new IconManager(configManager, urlBuilder);
        this.writable = false;
        this.commandParser = Optional.empty();
        this.tabCompleter = Optional.empty();
        this.messageSinkManager = Optional.empty();
        this.backBufferFactory = backBufferFactory;

        eventBusManager = new ChildEventBusManager(eventBus);
        eventBusManager.connect();
        this.eventBus = eventBusManager.getChildBus();
        this.unreadStatusManager = new UnreadStatusManager(this);
        this.eventBus.subscribe(unreadStatusManager);

        setIcon(icon);
    }

    /**
     * Instantiate new frame container that accepts user input.
     */
    protected FrameContainer(
            @Nullable final FrameContainer parent,
            final String icon,
            final String name,
            final String title,
            final AggregateConfigProvider config,
            final BackBufferFactory backBufferFactory,
            final URLBuilder urlBuilder,
            final CommandParser commandParser,
            final TabCompleter tabCompleter,
            final MessageSinkManager messageSinkManager,
            final DMDircMBassador eventBus,
            final Collection<String> components) {
        this.parent = Optional.ofNullable(parent);
        this.configManager = config;
        this.name = name;
        this.title = title;
        this.components = new HashSet<>(components);
        this.iconManager = new IconManager(configManager, urlBuilder);
        this.writable = true;
        this.commandParser = Optional.of(commandParser);
        this.tabCompleter = Optional.of(tabCompleter);
        this.messageSinkManager = Optional.of(messageSinkManager);
        this.backBufferFactory = backBufferFactory;
        commandParser.setOwner(this);

        eventBusManager = new ChildEventBusManager(eventBus);
        eventBusManager.connect();
        this.eventBus = eventBusManager.getChildBus();
        this.unreadStatusManager = new UnreadStatusManager(this);
        this.eventBus.subscribe(unreadStatusManager);

        setIcon(icon);
    }

    public Optional<Colour> getNotification() {
        return notification;
    }

    public Optional<FrameContainer> getParent() {
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

    public DMDircMBassador getEventBus() {
        return eventBus;
    }

    public boolean isWritable() {
        return writable;
    }

    /**
     * Returns a collection of direct children of this frame.
     *
     * @return This frame's children
     *
     * @since 0.6.4
     */
    public Collection<FrameContainer> getChildren() {
        return Collections.unmodifiableCollection(children);
    }

    /**
     * Adds a new child window to this frame.
     *
     * @param child The window to be added
     *
     * @since 0.6.4
     */
    public void addChild(final FrameContainer child) {
        children.add(child);
    }

    /**
     * Removes a child window from this frame.
     *
     * @param child The window to be removed
     *
     * @since 0.6.4
     */
    public void removeChild(final FrameContainer child) {
        children.remove(child);
    }

    /**
     * Gets an icon manager for this container.
     *
     * @return An icon manager for this container.
     */
    public IconManager getIconManager() {
        return iconManager;
    }

    /**
     * Retrieves the {@link IRCDocument} used to store this frame's content.
     *
     * @return This frame's document
     *
     * @since 0.6.4
     * @deprecated Use {@link #getBackBuffer()}
     */
    @Deprecated
    public IRCDocument getDocument() {
        return getBackBuffer().getDocument();
    }

    /**
     * Changes the name of this container, and fires a {@link FrameNameChangedEvent}.
     *
     * @param name The new name for this frame.
     */
    protected void setName(final String name) {
        this.name = name;

        eventBus.publishAsync(new FrameNameChangedEvent(this, name));
    }

    /**
     * Changes the title of this container, and fires a {@link FrameTitleChangedEvent}.
     *
     * @param title The new title for this frame.
     */
    public void setTitle(final String title) {
        this.title = title;

        eventBus.publishAsync(new FrameTitleChangedEvent(this, title));
    }

    /**
     * Returns the collection of UI component identifiers that this frame container requires for its
     * display.
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
        eventBus.publishAsync(new FrameComponentAddedEvent(this, component));
    }

    /**
     * Removes a component from this container.
     *
     * @since 0.6.6
     * @param component The component to be removed
     */
    public void removeComponent(final String component) {
        components.remove(component);

        eventBus.publishAsync(new FrameComponentRemovedEvent(this, component));
    }

    /**
     * Closes this container (and its associated frame).
     */
    public void close() {
        eventBus.unsubscribe(unreadStatusManager);
        eventBus.publish(new FrameClosingEvent(this));
        eventBusManager.disconnect();
        getBackBuffer().stopAddingEvents();
    }

    /**
     * Returns the connection that this container is associated with.
     *
     * @return the associated connection.
     */
    public abstract Optional<Connection> getConnection();

    /**
     * Sets the icon to be used by this frame container and fires a {@link FrameIconChangedEvent}.
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
        eventBus.publish(new FrameIconChangedEvent(this, icon));
    }

    /**
     * Retrieves the styliser which should be used by this container.
     *
     * @return this container's styliser
     * @deprecated Use {@link #getBackBuffer()}
     */
    @Deprecated
    public Styliser getStyliser() {
        return getBackBuffer().getStyliser();
    }

    /**
     * Gets the back buffer for this container.
     *
     * @return This container's back buffer.
     */
    public BackBuffer getBackBuffer() {
        synchronized (backBufferLock) {
            if (backBuffer == null) {
                backBuffer = backBufferFactory.getBackBuffer(this);
                backBuffer.startAddingEvents();
            }
        }

        return backBuffer;
    }

    /**
     * Clears any outstanding notifications this frame has set.
     */
    public void clearNotification() {
        notification = Optional.empty();
        eventBus.publishAsync(new NotificationClearedEvent(this));
    }

    /**
     * Sends a notification to the frame manager if this fame isn't active.
     *
     * @param colour The colour to use for the notification
     */
    public void sendNotification(final Colour colour) {
        if (!notification.isPresent() || !colour.equals(notification.get())) {
            notification = Optional.of(colour);
            eventBus.publishAsync(new NotificationSetEvent(this, colour));
        }
    }

    /**
     * Adds a line to this container's window. If the window is null for some reason, the line is
     * silently discarded.
     *
     * @param type      The message type to use
     * @param timestamp The timestamp to use for this line
     * @param args      The message's arguments
     *
     * @since 0.6.4
     */
    public void addLine(final String type, final Date timestamp, final Object... args) {
        if (type != null && !type.isEmpty()) {
            addLine(Formatter.formatMessage(getConfigManager(), type, args), timestamp);
        }
    }

    /**
     * Adds a line to this container's window. If the window is null for some reason, the line is
     * silently discarded.
     *
     * @param type The message type to use
     * @param args The message's arguments
     */
    public void addLine(final String type, final Object... args) {
        addLine(type, new Date(), args);
    }

    /**
     * Adds a line to this container's window. If the window is null for some reason, the line is
     * silently discarded.
     *
     * @param type      The message type to use
     * @param timestamp The timestamp to use for this line
     * @param args      The message's arguments
     *
     * @since 0.6.4
     */
    public void addLine(final StringBuffer type, final Date timestamp, final Object... args) {
        if (type != null) {
            addLine(type.toString(), timestamp, args);
        }
    }

    /**
     * Adds a line to this container's window. If the window is null for some reason, the line is
     * silently discarded.
     *
     * @param type The message type to use
     * @param args The message's arguments
     */
    public void addLine(final StringBuffer type, final Object... args) {
        addLine(type, new Date(), args);
    }

    /**
     * Adds the specified raw line to the window, without using a formatter.
     *
     * @param line      The line to be added
     * @param timestamp Whether or not to display the timestamp for this line
     */
    public void addLine(final String line, final boolean timestamp) {
        addLine(line, timestamp ? new Date() : null);
    }

    /**
     * Adds the specified raw line to the window, without using a formatter, and using the specified
     * timestamp. If the timestamp is <code>null</code>, no timestamp is added.
     *
     * @param line      The line to be added
     * @param timestamp The timestamp to use for the line
     *
     * @since 0.6.4
     */
    public void addLine(final String line, final Date timestamp) {
        for (final String myLine : line.split("\n")) {
            getBackBuffer().getDocument().addText(
                    timestamp.getTime(), DisplayPropertyMap.EMPTY, myLine);
            eventBus.publishAsync(new ClientLineAddedEvent(this, myLine));
        }
    }

    /**
     * Sends a line of text to this container's source.
     *
     * @param line The line to be sent
     */
    public void sendLine(final String line) {
        throw new UnsupportedOperationException("Container doesn't override sendLine");
    }

    /**
     * Retrieves the command parser to be used for this container.
     *
     * @return This container's command parser
     */
    public CommandParser getCommandParser() {
        checkState(writable);
        return commandParser.get();
    }

    /**
     * Retrieves the tab completer which should be used for this container.
     *
     * @return This container's tab completer
     */
    public TabCompleter getTabCompleter() {
        checkState(writable);
        return tabCompleter.get();
    }

    /**
     * Returns the maximum length that a line passed to sendLine() should be, in order to prevent it
     * being truncated or causing protocol violations.
     *
     * @return The maximum line length for this container
     */
    public int getMaxLineLength() {
        throw new UnsupportedOperationException("Container doesn't override getMaxLineLength");
    }

    /**
     * Splits the specified line into chunks that contain a number of bytes less than or equal to
     * the value returned by {@link #getMaxLineLength()}.
     *
     * @param line The line to be split
     *
     * @return An ordered list of chunks of the desired length
     */
    protected List<String> splitLine(final String line) {
        final List<String> result = new ArrayList<>();

        if (line.indexOf('\n') > -1) {
            for (String part : line.split("\n")) {
                result.addAll(splitLine(part));
            }
        } else {
            final StringBuilder remaining = new StringBuilder(line);

            while (getMaxLineLength() > -1 && remaining.toString().getBytes().length
                    > getMaxLineLength()) {
                int number = Math.min(remaining.length(), getMaxLineLength());

                while (remaining.substring(0, number).getBytes().length > getMaxLineLength()) {
                    number--;
                }

                result.add(remaining.substring(0, number));
                remaining.delete(0, number);
            }

            result.add(remaining.toString());
        }

        return result;
    }

    /**
     * Returns the number of lines that the specified string would be sent as.
     *
     * @param line The string to be split and sent
     *
     * @return The number of lines required to send the specified string
     */
    public final int getNumLines(final String line) {
        final String[] splitLines = line.split("(\n|\r\n|\r)", Integer.MAX_VALUE);
        int lines = 0;

        for (String splitLine : splitLines) {
            if (getMaxLineLength() <= 0) {
                lines++;
            } else {
                lines += (int) Math.ceil(splitLine.getBytes().length
                        / (double) getMaxLineLength());
            }
        }

        return lines;
    }

    /**
     * Processes and displays a notification.
     *
     * @param messageType The name of the formatter to be used for the message
     * @param args        The arguments for the message
     *
     * @return True if any further behaviour should be executed, false otherwise
     */
    public boolean doNotification(final String messageType, final Object... args) {
        return doNotification(new Date(), messageType, args);
    }

    /**
     * Processes and displays a notification.
     *
     * @param date        The date/time at which the event occurred
     * @param messageType The name of the formatter to be used for the message
     * @param args        The arguments for the message
     *
     * @return True if any further behaviour should be executed, false otherwise
     */
    public boolean doNotification(final Date date, final String messageType, final Object... args) {
        final List<Object> messageArgs = new ArrayList<>();
        final List<Object> actionArgs = new ArrayList<>();

        actionArgs.add(this);

        for (Object arg : args) {
            actionArgs.add(arg);

            if (!processNotificationArg(arg, messageArgs)) {
                messageArgs.add(arg);
            }
        }

        modifyNotificationArgs(actionArgs, messageArgs);

        handleNotification(date, messageType, messageArgs.toArray());

        return true;
    }

    /**
     * Allows subclasses to modify the lists of arguments for notifications.
     *
     * @param actionArgs  The list of arguments to be passed to the actions system
     * @param messageArgs The list of arguments to be passed to the formatter
     */
    protected void modifyNotificationArgs(final List<Object> actionArgs,
            final List<Object> messageArgs) {
        // Do nothing
    }

    /**
     * Allows subclasses to process specific types of notification arguments.
     *
     * @param arg  The argument to be processed
     * @param args The list of arguments that any data should be appended to
     *
     * @return True if the arg has been processed, false otherwise
     */
    protected boolean processNotificationArg(final Object arg, final List<Object> args) {
        return false;
    }

    /**
     * Handles general server notifications (i.e., ones not tied to a specific window). The user can
     * select where the notifications should go in their config.
     *
     * @param messageType The type of message that is being sent
     * @param args        The arguments for the message
     */
    public void handleNotification(final String messageType, final Object... args) {
        handleNotification(new Date(), messageType, args);
    }

    /**
     * Handles general server notifications (i.e., ones not tied to a specific window). The user can
     * select where the notifications should go in their config.
     *
     * @param date        The date/time at which the event occurred
     * @param messageType The type of message that is being sent
     * @param args        The arguments for the message
     */
    public void handleNotification(final Date date, final String messageType, final Object... args) {
        checkState(writable);
        messageSinkManager.get().dispatchMessage(this, date, messageType, args);
    }

    /**
     * Sets the composition state for the local user for this chat.
     *
     * @param state The new composition state
     */
    public void setCompositionState(final CompositionState state) {
        // Default implementation does nothing. Subclasses that support
        // composition should override this.
    }

    public UnreadStatusManager getUnreadStatusManager() {
        return unreadStatusManager;
    }

}
