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

package com.dmdirc.util;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.listener.Handler;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility for creating and managing instances of {@link net.engio.mbassy.bus.MBassador} that are slaved to a parent bus.
 * <p>
 * Any events sent on the child bus will be propagated up to the parent bus.
 */
public class ChildEventBusManager {

    private final MBassador parent;
    private final MBassador child;
    private final EventPropagator propagator;

    public ChildEventBusManager(final MBassador parent) {
        this.parent = checkNotNull(parent);
        this.child = new MBassador(BusConfiguration.Default());
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
     * After this method is called, no further events will be passed to the parent.
     */
    public void disconnect() {
        child.unsubscribe(propagator);
    }

    /**
     * Gets the child bus.
     *
     * @return The child bus belonging to this manager.
     */
    public MBassador getChildBus() {
        return child;
    }

    private class EventPropagator {

        @Handler
        public void handleEvent(final Object object) {
            parent.post(object);
        }

    }

}
