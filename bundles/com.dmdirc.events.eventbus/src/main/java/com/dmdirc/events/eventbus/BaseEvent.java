/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.events.eventbus;

import java.time.LocalDateTime;

/**
 * Base class for all events processed by an {@link EventBus}.
 */
public abstract class BaseEvent {

    /**
     * The time the event was triggered at.
     */
    private final LocalDateTime timestamp;

    /**
     * Creates a new event with the specified timestamp.
     *
     * @param timestamp The stamp the event occurred at.
     */
    protected BaseEvent(final LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Creates a new event with the current system timestamp.
     */
    protected BaseEvent() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Gets the timestamp at which the event occurred.
     *
     * <p>This may be prior to the creation time of the event, or of the entire application, if the event is a
     * historical one (e.g. after connecting to a server that replays missed messages).
     *
     * @return The time at which the event occurred.
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

}
