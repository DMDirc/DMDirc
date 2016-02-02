/*
 * Copyright (c) 2006-2015 DMDirc Developers
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
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.parser.common.CompositionState;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.messages.BackBuffer;
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.ui.messages.Formatter;
import com.dmdirc.ui.messages.UnreadStatusManager;
import com.dmdirc.ui.messages.sink.MessageSinkManager;
import com.dmdirc.util.ChildEventBusManager;
import com.dmdirc.util.collections.ListenerList;

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
public abstract class FrameContainer implements WindowModel {

    /** Listeners not yet using ListenerSupport. */
    protected final ListenerList listeners = new ListenerList();
    /** The children of this frame. */
    private final Collection<WindowModel> children = new CopyOnWriteArrayList<>();
    /** The parent of this frame. */
    private final Optional<WindowModel> parent;
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
    /** The manager handling this frame's unread status. */
    private final UnreadStatusManager unreadStatusManager;
    /** Whether or not this container is writable. */
    private final boolean writable;
    /** The back buffer factory. */
    private final BackBufferFactory backBufferFactory;
    /** The back buffer for this container. */
    private BackBuffer backBuffer;

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
     * The command parser used for commands in this container.
     * <p>
     * Only defined if this container is {@link #writable}.
     */
    private Optional<CommandParser> commandParser = Optional.empty();

    /**
     * Instantiate new frame container.
     */
    protected FrameContainer(
            @Nullable final WindowModel parent,
            final String icon,
            final String name,
            final String title,
            final AggregateConfigProvider config,
            final BackBufferFactory backBufferFactory,
            final DMDircMBassador eventBus,
            final Collection<String> components) {
        this.parent = Optional.ofNullable(parent);
        this.configManager = config;
        this.name = name;
        this.title = title;
        this.components = new HashSet<>(components);
        this.writable = false;
        this.tabCompleter = Optional.empty();
        this.messageSinkManager = Optional.empty();
        this.backBufferFactory = backBufferFactory;

        eventBusManager = new ChildEventBusManager(eventBus);
        eventBusManager.connect();
        this.eventBus = eventBusManager.getChildBus();
        this.unreadStatusManager = new UnreadStatusManager(this);
        this.eventBus.subscribe(unreadStatusManager);
        configManager.getBinder().bind(unreadStatusManager, UnreadStatusManager.class);

        setIcon(icon);
    }

    /**
     * Instantiate new frame container that accepts user input.
     */
    protected FrameContainer(
            @Nullable final WindowModel parent,
            final String icon,
            final String name,
            final String title,
            final AggregateConfigProvider config,
            final BackBufferFactory backBufferFactory,
            final TabCompleter tabCompleter,
            final MessageSinkManager messageSinkManager,
            final DMDircMBassador eventBus,
            final Collection<String> components) {
        this.parent = Optional.ofNullable(parent);
        this.configManager = config;
        this.name = name;
        this.title = title;
        this.components = new HashSet<>(components);
        this.writable = true;
        this.tabCompleter = Optional.of(tabCompleter);
        this.messageSinkManager = Optional.of(messageSinkManager);
        this.backBufferFactory = backBufferFactory;

        eventBusManager = new ChildEventBusManager(eventBus);
        eventBusManager.connect();
        this.eventBus = eventBusManager.getChildBus();
        this.unreadStatusManager = new UnreadStatusManager(this);
        this.eventBus.subscribe(unreadStatusManager);
        configManager.getBinder().bind(unreadStatusManager, UnreadStatusManager.class);

        setIcon(icon);
    }

    protected void initBackBuffer() {
        backBuffer = backBufferFactory.getBackBuffer(this);
        backBuffer.startAddingEvents();
    }

    public void setCommandParser(final CommandParser commandParser) {
        this.commandParser = Optional.ofNullable(commandParser);
    }

    @Override
    public Optional<WindowModel> getParent() {
        return parent;
    }

    @Override
    public String getIcon() {
        return icon;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public AggregateConfigProvider getConfigManager() {
        return configManager;
    }

    @Override
    public DMDircMBassador getEventBus() {
        return eventBus;
    }

    @Override
    public boolean isWritable() {
        return writable;
    }

    @Override
    public Collection<WindowModel> getChildren() {
        return Collections.unmodifiableCollection(children);
    }

    @Override
    public void addChild(final WindowModel child) {
        children.add(child);
    }

    @Override
    public void removeChild(final WindowModel child) {
        children.remove(child);
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

    @Override
    public void setTitle(final String title) {
        this.title = title;

        eventBus.publishAsync(new FrameTitleChangedEvent(this, title));
    }

    @Override
    public Set<String> getComponents() {
        return Collections.unmodifiableSet(components);
    }

    @Override
    public void addComponent(final String component) {
        components.add(component);
        eventBus.publishAsync(new FrameComponentAddedEvent(this, component));
    }

    @Override
    public void removeComponent(final String component) {
        components.remove(component);

        eventBus.publishAsync(new FrameComponentRemovedEvent(this, component));
    }

    @Override
    public void close() {
        eventBus.unsubscribe(unreadStatusManager);
        configManager.getBinder().unbind(unreadStatusManager);
        eventBus.publish(new FrameClosingEvent(this));
        eventBusManager.disconnect();
        getBackBuffer().stopAddingEvents();
    }

    @Override
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

    @Override
    public BackBuffer getBackBuffer() {
        return backBuffer;
    }

    @Override
    public void addLine(final String type, final Date timestamp, final Object... args) {
        if (type != null && !type.isEmpty()) {
            addLine(Formatter.formatMessage(getConfigManager(), type, args), timestamp);
        }
    }

    @Override
    public void addLine(final String type, final Object... args) {
        addLine(type, new Date(), args);
    }

    @Override
    public void addLine(final StringBuffer type, final Date timestamp, final Object... args) {
        if (type != null) {
            addLine(type.toString(), timestamp, args);
        }
    }

    @Override
    public void addLine(final StringBuffer type, final Object... args) {
        addLine(type, new Date(), args);
    }

    @Override
    @Deprecated
    public void addLine(final String line, final boolean timestamp) {
        addLine(line, new Date());
    }

    @Override
    public void addLine(final String line, final Date timestamp) {
        for (final String myLine : line.split("\n")) {
            getBackBuffer().getDocument().addText(
                    timestamp.getTime(), DisplayPropertyMap.EMPTY, myLine);
            eventBus.publishAsync(new ClientLineAddedEvent(this, myLine));
        }
    }

    @Override
    public void sendLine(final String line) {
        throw new UnsupportedOperationException("Container doesn't override sendLine");
    }

    @Override
    public CommandParser getCommandParser() {
        checkState(writable);
        return commandParser.get();
    }

    @Override
    public TabCompleter getTabCompleter() {
        checkState(writable);
        return tabCompleter.get();
    }

    @Override
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

    @Override
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

        handleNotification(new Date(), messageType, messageArgs.toArray());

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

    @Override
    public UnreadStatusManager getUnreadStatusManager() {
        return unreadStatusManager;
    }

}
