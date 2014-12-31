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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides a map of {@link DisplayProperty}s to values, maintaining type safety.
 */
public class DisplayPropertyMap {

    public static final DisplayPropertyMap EMPTY = new ReadOnlyDisplayPropertyMap();

    private final Map<DisplayProperty<?>, Object> properties = new ConcurrentHashMap<>();

    /**
     * Gets the value of the specified property, if present.
     *
     * @param property The property to be retrieved.
     * @param <T> The type of value the property takes.
     * @return An optional containing the value of the property if it was present.
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(final DisplayProperty<T> property) {
        return Optional.ofNullable((T) properties.get(property));
    }

    /**
     * Adds a new value for the specified property. Any previous value will be replaced.
     *
     * @param property The property to set.
     * @param value The new value of the property.
     * @param <T> The type of value the property takes.
     */
    public <T> void put(final DisplayProperty<T> property, final T value) {
        properties.put(property, value);
    }

    /**
     * Removes a value for the specified property.
     *
     * @param property The property to be removed
     * @param <T> The type of value the property takes
     */
    public <T> void remove(final DisplayProperty<T> property) {
        properties.remove(property);
    }

    private static class ReadOnlyDisplayPropertyMap extends DisplayPropertyMap {
        @Override
        public <T> void put(final DisplayProperty<T> property, final T value) {
            throw new UnsupportedOperationException("Read only");
        }
    }

}
