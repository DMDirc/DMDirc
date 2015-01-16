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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.events.FrameIconChangedEvent;
import com.dmdirc.events.FrameTitleChangedEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.messages.BackBuffer;
import com.dmdirc.ui.messages.UnreadStatusManager;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

/**
 * Models the state of a window.
 */
public interface WindowModel {

    Optional<FrameContainer> getParent();

    String getIcon();

    String getName();

    String getTitle();

    AggregateConfigProvider getConfigManager();

    DMDircMBassador getEventBus();

    boolean isWritable();

    /**
     * Returns a collection of direct children of this frame.
     *
     * @return This frame's children
     *
     * @since 0.6.4
     */
    Collection<FrameContainer> getChildren();

    /**
     * Adds a new child window to this frame.
     *
     * @param child The window to be added
     *
     * @since 0.6.4
     */
    void addChild(FrameContainer child);

    /**
     * Removes a child window from this frame.
     *
     * @param child The window to be removed
     *
     * @since 0.6.4
     */
    void removeChild(FrameContainer child);

    /**
     * Changes the title of this container, and fires a {@link FrameTitleChangedEvent}.
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
     * Sets the icon to be used by this frame container and fires a {@link FrameIconChangedEvent}.
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
     * Adds a line to this container's window. If the window is null for some reason, the line is
     * silently discarded.
     *
     * @param type      The message type to use
     * @param timestamp The timestamp to use for this line
     * @param args      The message's arguments
     *
     * @since 0.6.4
     */
    void addLine(String type, Date timestamp, Object... args);

    /**
     * Adds a line to this container's window. If the window is null for some reason, the line is
     * silently discarded.
     *
     * @param type The message type to use
     * @param args The message's arguments
     */
    void addLine(String type, Object... args);

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
    void addLine(StringBuffer type, Date timestamp, Object... args);

    /**
     * Adds a line to this container's window. If the window is null for some reason, the line is
     * silently discarded.
     *
     * @param type The message type to use
     * @param args The message's arguments
     */
    void addLine(StringBuffer type, Object... args);

    /**
     * Adds the specified raw line to the window, without using a formatter.
     *
     * @param line      The line to be added
     * @param timestamp Whether or not to display the timestamp for this line
     * @deprecated Timestamps are always displayed.
     */
    @Deprecated
    void addLine(String line, boolean timestamp);

    /**
     * Adds the specified raw line to the window, without using a formatter, and using the specified
     * timestamp. If the timestamp is <code>null</code>, no timestamp is added.
     *
     * @param line      The line to be added
     * @param timestamp The timestamp to use for the line
     *
     * @since 0.6.4
     */
    void addLine(String line, Date timestamp);

    /**
     * Sends a line of text to this container's source.
     *
     * @param line The line to be sent
     */
    void sendLine(String line);

    /**
     * Retrieves the command parser to be used for this container.
     *
     * @return This container's command parser
     */
    CommandParser getCommandParser();

    /**
     * Retrieves the tab completer which should be used for this container.
     *
     * @return This container's tab completer
     */
    TabCompleter getTabCompleter();

    /**
     * Returns the maximum length that a line passed to sendLine() should be, in order to prevent it
     * being truncated or causing protocol violations.
     *
     * @return The maximum line length for this container
     */
    int getMaxLineLength();

    /**
     * Returns the number of lines that the specified string would be sent as.
     *
     * @param line The string to be split and sent
     *
     * @return The number of lines required to send the specified string
     */
    int getNumLines(String line);

    UnreadStatusManager getUnreadStatusManager();
}
