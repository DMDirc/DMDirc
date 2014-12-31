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

import org.junit.Before;
import org.junit.Test;

import net.engio.mbassy.listener.Handler;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class ChildEventBusManagerTest {

    private ChildEventBusManager manager;
    private StubHandler handler;

    @Before
    public void setup() {
        final DMDircMBassador parent = new DMDircMBassador();
        manager = new ChildEventBusManager(parent);
        handler = new StubHandler();
        parent.subscribe(handler);
    }

    @Test
    public void testDoesNotPropagateBeforeConnect() {
        manager.getChildBus().publish(new StubEvent());
        assertNull(handler.received);
    }

    @Test
    public void testDoesNotPropagateAfterDisconnect() throws InterruptedException {
        manager.connect();
        manager.disconnect();

        // Wait for the asynchronous disconnect to go through. This is a bit lame.
        do {
            Thread.sleep(100);
        } while (manager.getChildBus().hasPendingMessages());

        manager.getChildBus().publish(new StubEvent());
        assertNull(handler.received);
    }

    @Test
    public void testPropagates() {
        final DMDircEvent stubEvent = new StubEvent();

        manager.connect();
        manager.getChildBus().publish(stubEvent);
        assertSame(stubEvent, handler.received);
    }

    private static class StubHandler {

        public DMDircEvent received;

        @Handler
        public void handleEvent(final DMDircEvent event) {
            received = event;
        }

    }

    private static class StubEvent extends DMDircEvent {

    }

}