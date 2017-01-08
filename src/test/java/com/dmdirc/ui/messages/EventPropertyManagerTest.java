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

package com.dmdirc.ui.messages;

import com.dmdirc.Channel;
import com.dmdirc.events.ChannelMessageEvent;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.GroupChatUser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

@RunWith(MockitoJUnitRunner.class)
public class EventPropertyManagerTest {

    private EventPropertyManager manager;

    @Mock private EventBus eventBus;
    @Mock private Channel channel;
    @Mock private GroupChatUser client;

    @Before
    public void setUp() {
        manager = new EventPropertyManager();
    }

    @Test
    public void testAppliesBuiltInFunctions() {
        assertEquals("test 123", manager.applyFunction("TeSt 123", "lowercase"));
        assertEquals("TEST 123", manager.applyFunction("TeSt 123", "uppercase"));
        assertEquals("TeSt 123", manager.applyFunction("  TeSt 123  ", "trim"));
        assertEquals("TeSt 123", manager.applyFunction("\2TeSt \4FFFFFF123", "unstyled"));
    }

    @Test
    public void testGetsProperties() {
        final String message = "a message";
        final ChannelMessageEvent event = new ChannelMessageEvent(channel, client, message);

        assertSame(message, manager.getProperty(event, ChannelMessageEvent.class, "message").get());
        assertSame(channel, manager.getProperty(event, ChannelMessageEvent.class, "channel").get());
        assertSame(client, manager.getProperty(event, ChannelMessageEvent.class, "client").get());
    }

    @Test
    public void testNonExistantProperties() {
        assertFalse(manager.getProperty(new Object(), Object.class, "foobar").isPresent());
    }

}