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

import com.dmdirc.events.FrameClosingEvent;
import com.dmdirc.events.FrameComponentAddedEvent;
import com.dmdirc.events.FrameComponentRemovedEvent;
import com.dmdirc.events.FrameIconChangedEvent;
import com.dmdirc.events.FrameNameChangedEvent;
import com.dmdirc.events.FrameTitleChangedEvent;
import com.dmdirc.interfaces.InputModel;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.parser.common.CompositionState;
import com.dmdirc.ui.messages.BackBuffer;
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.ui.messages.UnreadStatusManager;
import com.dmdirc.util.ChildEventBusManager;
import com.dmdirc.util.collections.ListenerList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * The frame container implements basic methods that should be present in all objects that handle a
 * frame.
 */
public abstract class FrameContainer implements WindowModel {

    /** Listeners not yet using ListenerSupport. */
    protected final ListenerList listeners = new ListenerList();
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
    /** The back buffer factory. */
    private final BackBufferFactory backBufferFactory;
    /** The back buffer for this container. */
    private BackBuffer backBuffer;
    /** The input model for this container. */
    private Optional<InputModel> inputModel = Optional.empty();

    /**
     * Instantiate new frame container.
     */
    protected FrameContainer(
            final String icon,
            final String name,
            final String title,
            final AggregateConfigProvider config,
            final BackBufferFactory backBufferFactory,
            final DMDircMBassador eventBus,
            final Collection<String> components) {
        this.configManager = config;
        this.name = name;
        this.title = title;
        this.components = new HashSet<>(components);
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
    public Optional<InputModel> getInputModel() {
        return inputModel;
    }

    /**
     * Sets an input model for this window. If a window does not have an input model, then it
     * will not accept user input.
     *
     * @param inputModel The new input model to use (null to disallow input).
     */
    public void setInputModel(@Nullable final InputModel inputModel) {
        this.inputModel = Optional.ofNullable(inputModel);
    }

    @Override
    public UnreadStatusManager getUnreadStatusManager() {
        return unreadStatusManager;
    }

}
