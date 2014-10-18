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
    @Mock private EventTemplateProvider templateProvider;
    @Mock private Channel channel;
    private ChannelMessageEvent messageEvent;
    private EventFormatter formatter;

    @Before
    public void setup() {
        formatter = new EventFormatter(propertyManager, templateProvider);
        messageEvent = new ChannelMessageEvent(channel, null, null);

        when(templateProvider.getTemplate(ChannelMessageEvent.class))
                .thenReturn(Optional.ofNullable("Template {{channel}} meep"));
        when(propertyManager.getProperty(messageEvent, ChannelMessageEvent.class, "channel"))
                .thenReturn("MONKEY");
    }

    @Test
    public void testBasicFormat() {
        assertEquals("Template MONKEY meep", formatter.format(messageEvent).orElse(null));
    }

}
