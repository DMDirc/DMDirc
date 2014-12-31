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

package com.dmdirc.util;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.DMDircEvent;

import net.engio.mbassy.listener.Handler;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility for creating and managing instances of {@link DMDircMBassador} that are slaved to a
 * parent bus.
 * <p>
 * Any events sent on the child bus will be propagated up to the parent bus.
 */
public class ChildEventBusManager {

    private final DMDircMBassador parent;
    private final DMDircMBassador child;
    private final EventPropagator propagator;

    public ChildEventBusManager(final DMDircMBassador parent) {
        this.parent = checkNotNull(parent);
        this.child = new DMDircMBassador();
        this.propagator = new EventPropagator();
    }

    /**
     * Connects the child event bus to the parent.
     * <p>
     * After this method is called, all events sent to the child bus will be passed to the parent.
     */
    public void connect() {
        child.subscribe(propagator);
    }

    /**
     * Disconnects the child event bus from the parent.
     * <p>
     * The child will be disconnected asynchronously, to allow any pending async events to be
     * dispatched. After the bus is disconnected, no future events will be passed to the parent.
     */
    public void disconnect() {
        child.publishAsync(new ChildEventBusDisconnectingEvent());
    }

    /**
     * Gets the child bus.
     *
     * @return The child bus belonging to this manager.
     */
    public DMDircMBassador getChildBus() {
        return child;
    }

    private class EventPropagator {

        @Handler
        public void handleEvent(final DMDircEvent event) {
            if (!(event instanceof ChildEventBusDisconnectingEvent)) {
                // Don't propagate our private event
                parent.publish(event);
            }
        }

        // Allow all other handlers on the child bus to process this first
        @Handler(priority = EventUtils.PRIORITY_LOWEST)
        public void handleDisconnect(final ChildEventBusDisconnectingEvent event) {
            child.unsubscribe(propagator);
        }

    }

    private static class ChildEventBusDisconnectingEvent extends DMDircEvent {

    }

}
