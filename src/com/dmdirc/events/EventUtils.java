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

import com.dmdirc.DMDircMBassador;

/**
 * Utilities for interacting with events.
 */
public final class EventUtils {

    private EventUtils() {
    }

    /**
     * Posts a displayable event to the bus, and returns the updated display format.
     *
     * @param eventBus      The bus to post events to.
     * @param event         The event to be posted.
     * @param displayFormat The initial, default, display format.
     *
     * @return The event's display format after it has been posted on the event bus.
     */
    public static <T extends DMDircEvent & DisplayableEvent> String postDisplayable(
            final DMDircMBassador eventBus,
            final T event,
            final String displayFormat) {
        event.setDisplayFormat(displayFormat);
        eventBus.publish(event);
        return event.getDisplayFormat();
    }

}
