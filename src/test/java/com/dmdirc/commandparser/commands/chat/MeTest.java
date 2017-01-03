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
package com.dmdirc.commandparser.commands.chat;

import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.ChatCommandContext;
import com.dmdirc.events.CommandErrorEvent;
import com.dmdirc.interfaces.Chat;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.WindowModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeTest {

    @Mock private Chat chat;
    @Mock private WindowModel frameContainer;
    @Mock private EventBus eventbus;
    @Mock private CommandController controller;
    private Me command;

    @Before
    public void setUp() {
        when(frameContainer.getEventBus()).thenReturn(eventbus);
        command = new Me(controller);
    }

    @Test
    public void testUsage() {
        command.execute(frameContainer, new CommandArguments(controller, "/foo"),
                new ChatCommandContext(null, Me.INFO, chat));
        verify(eventbus).publishAsync(isA(CommandErrorEvent.class));
    }

    @Test
    public void testSend() {
        command.execute(null, new CommandArguments(controller, "/foo hello meep moop"),
                new ChatCommandContext(null, Me.INFO, chat));

        verify(chat).sendAction("hello meep moop");
    }
}