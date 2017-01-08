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

package com.dmdirc.interfaces;

import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.ui.messages.BackBuffer;
import com.dmdirc.ui.messages.UnreadStatusManager;

import java.util.Optional;
import java.util.Set;

/**
 * Models the state of a window.
 */
public interface WindowModel {

    /**
     * Gets a unique ID that identifies this window.
     *
     * @return This window's unique ID.
     */
    String getId();

    /**
     * Sets the ID for this window. This may only be called once; attempting to overwrite a
     * previous ID will throw an exception.
     *
     * @param id The new ID for this window.
     */
    void setId(String id);

    String getIcon();

    String getName();

    String getTitle();

    AggregateConfigProvider getConfigManager();

    EventBus getEventBus();

    /**
     * Changes the name of this container.
     *
     * @param name The new name for this frame.
     */
    void setName(String name);

    /**
     * Changes the title of this container.
     *
     * @param title The new title for this frame.
     */
    void setTitle(String title);

    /**
     * Returns the collection of UI component identifiers that this frame container requires for its
     * display.
     *
     * @since 0.6.6
     * @return Collection of UI component identifiers
     */
    Set<String> getComponents();

    /**
     * Adds a new component to this container.
     *
     * @since 0.6.6
     * @param component The component to be added
     */
    void addComponent(String component);

    /**
     * Removes a component from this container.
     *
     * @since 0.6.6
     * @param component The component to be removed
     */
    void removeComponent(String component);

    /**
     * Closes this container (and its associated frame).
     */
    void close();

    /**
     * Returns the connection that this container is associated with.
     *
     * @return the associated connection.
     */
    Optional<Connection> getConnection();

    /**
     * Sets the icon to be used by this window.
     *
     * @param icon The new icon to be used
     */
    void setIcon(String icon);

    /**
     * Gets the back buffer for this container.
     *
     * @return This container's back buffer.
     */
    BackBuffer getBackBuffer();

    /**
     * Returns the model used to define input parameters for this window.
     *
     * @return If this window accepts input, the input model to use, otherwise an empty optional.
     */
    Optional<InputModel> getInputModel();

    /**
     * Returns the manager tracking this window's unread messages.
     *
     * @return The unread status manager for this window.
     */
    UnreadStatusManager getUnreadStatusManager();

}
