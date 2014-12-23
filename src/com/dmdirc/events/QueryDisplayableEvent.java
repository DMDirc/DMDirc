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

package com.dmdirc.events;

import com.dmdirc.Query;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base type for displayable events that occur in queries.
 */
public abstract class QueryDisplayableEvent extends QueryEvent implements
        DisplayableEvent {

    /** The display format to use for this event. */
    private final AtomicReference<String> displayFormatRef = new AtomicReference<>("");
    /** The properties associated with this event. */
    private final DisplayPropertyMap properties = new DisplayPropertyMap();

    public QueryDisplayableEvent(final long timestamp, final Query query) {
        super(timestamp, query);
    }

    public QueryDisplayableEvent(final Query query) {
        super(query);
    }

    @Override
    public String getDisplayFormat() {
        return displayFormatRef.get();
    }

    @Override
    public void setDisplayFormat(final String format) {
        displayFormatRef.set(format);
    }

    @Override
    public <T> void setDisplayProperty(final DisplayProperty<T> property, final T value) {
        properties.put(property, value);
    }

    @Override
    public <T> Optional<T> getDisplayProperty(final DisplayProperty<T> property) {
        return properties.get(property);
    }

    @Override
    public DisplayPropertyMap getDisplayProperties() {
        return properties;
    }
}
