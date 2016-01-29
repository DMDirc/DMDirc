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

package com.dmdirc.events;

import com.dmdirc.interfaces.WindowModel;

import java.util.Optional;

/**
 * Describes an event which is rendered in the client to the user.
 */
public interface DisplayableEvent {

    /**
     * Gets the format name that will be used to display the event.
     *
     * @return The format name to use for the event.
     */
    String getDisplayFormat();

    /**
     * Sets the format name that should be used to display the event.
     *
     * @param format The format name to use for the event.
     */
    void setDisplayFormat(String format);

    /**
     * Sets a property relating to how this event should be displayed.
     *
     * @param property The property to be set
     * @param value The value of the property
     * @param <T> The type of value that the property takes.
     */
    <T> void setDisplayProperty(DisplayProperty<T> property, T value);

    /**
     * Retrieves a property relating to how this event should be displayed.
     *
     * @param property The property to be retrieved.
     * @param <T> The type of value that the property takes.
     * @return An optional value for the property.
     */
    <T> Optional<T> getDisplayProperty(DisplayProperty<T> property);

    /**
     * Determines whether this event has a display property.
     *
     * <p>Only use this method if the value of the property does not matter; otherwise use
     * {@link #getDisplayProperty(DisplayProperty)} and use the appropriate {@link Optional}
     * accessors.
     *
     * @param property The property to be checked.
     * @return True if the property is present, false otherwise.
     */
    default boolean hasDisplayProperty(final DisplayProperty<?> property) {
        return getDisplayProperty(property).isPresent();
    }

    /**
     * Gets the map of all display properties.
     *
     * @return The map of display properties.
     */
    DisplayPropertyMap getDisplayProperties();

    /**
     * Gets the timestamp at which the event occurred.
     *
     * @return The timestamp the event occurred at.
     */
    long getTimestamp();

    /**
     * Gets the source of the displayable event.
     */
    WindowModel getSource();

}
