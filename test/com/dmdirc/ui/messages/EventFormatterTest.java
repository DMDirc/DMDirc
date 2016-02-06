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

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventFormatterTest {

    @Mock private EventPropertyManager propertyManager;
    @Mock private EventFormatProvider templateProvider;
    @Mock private Channel channel;
    private ChannelMessageEvent messageEvent;
    private EventFormatter formatter;

    @Before
    public void setup() {
        formatter = new EventFormatter(propertyManager, templateProvider);
    }

    @Test
    public void testBasicFormat() {
        messageEvent = new ChannelMessageEvent(channel, null, null);

        when(templateProvider.getFormat(ChannelMessageEvent.class))
                .thenReturn(Optional.of(
                        EventFormat.create(
                                "Template {{channel}} meep",
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty())));
        when(propertyManager.getProperty(messageEvent, ChannelMessageEvent.class, "channel"))
                .thenReturn(Optional.of("MONKEY"));

        assertEquals("Template MONKEY meep", formatter.format(messageEvent).orElse(null));
    }

    @Test
    public void testBeforeAndAfterFormat() {
        messageEvent = new ChannelMessageEvent(channel, null, null);

        when(templateProvider.getFormat(ChannelMessageEvent.class))
                .thenReturn(Optional.of(
                        EventFormat.create(
                                "Template {{channel}} meep",
                                Optional.of("Before!"),
                                Optional.of("After!"),
                                Optional.empty())));
        when(propertyManager.getProperty(messageEvent, ChannelMessageEvent.class, "channel"))
                .thenReturn(Optional.of("MONKEY"));

        assertEquals(
                "Before!\nTemplate MONKEY meep\nAfter!",
                formatter.format(messageEvent).orElse(null));
    }

    @Test
    public void testFormatWithFunction() {
        messageEvent = new ChannelMessageEvent(channel, null, null);

        when(templateProvider.getFormat(ChannelMessageEvent.class))
                .thenReturn(Optional.of(
                        EventFormat.create(
                                "Template {{channel|lowercase}} meep",
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty())));
        when(propertyManager.getProperty(messageEvent, ChannelMessageEvent.class, "channel"))
                .thenReturn(Optional.of("MONKEY"));
        when(propertyManager.applyFunction("MONKEY", "lowercase")).thenReturn("monkey");

        assertEquals("Template monkey meep", formatter.format(messageEvent).orElse(null));
    }

    @Test
    public void testFormatWithNestedSubstitutions() {
        messageEvent = new ChannelMessageEvent(channel, null, "{{channel}}");

        when(templateProvider.getFormat(ChannelMessageEvent.class))
                .thenReturn(Optional.of(
                        EventFormat.create(
                                "Template {{message}} meep",
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty())));
        when(propertyManager.getProperty(messageEvent, ChannelMessageEvent.class, "message"))
                .thenReturn(Optional.of("{{channel}}"));

        assertEquals("Template {{channel}} meep", formatter.format(messageEvent).orElse(null));
    }

}
