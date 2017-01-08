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

package com.dmdirc.commandparser.commands.channel;

import com.dmdirc.Channel;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.ChannelCommandContext;
import com.dmdirc.events.CommandErrorEvent;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.GroupChatUser;
import com.dmdirc.interfaces.User;
import com.dmdirc.interfaces.WindowModel;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KickReasonTest {

    @Mock private Connection connection;
    @Mock private User user1;
    @Mock private User user2;
    @Mock private GroupChatUser groupChatUser;
    @Mock private Channel channel;
    @Mock private CommandController controller;
    @Mock private WindowModel container;
    @Mock private EventBus eventbus;
    @Captor private ArgumentCaptor<CommandErrorEvent> errorEventCaptor;
    private KickReason command;

    @Before
    public void setup() {
        when(channel.getConnection()).thenReturn(Optional.of(connection));
        when(connection.getUser("user")).thenReturn(user1);
        when(connection.getUser("user1")).thenReturn(user2);
        when(channel.getUser(user1)).thenReturn(Optional.of(groupChatUser));
        when(channel.getUser(user2)).thenReturn(Optional.empty());
        when(controller.getCommandChar()).thenReturn('/');
        when(controller.getSilenceChar()).thenReturn('.');
        when(container.getEventBus()).thenReturn(eventbus);
        command = new KickReason(controller);
    }

    @Test
    public void testUsage() {
        command.execute(container, new CommandArguments(controller, "/kick"),
                new ChannelCommandContext(null, KickReason.INFO, channel));
        verify(eventbus).publishAsync(isA(CommandErrorEvent.class));
    }

    @Test
    public void testUnknown() {
        command.execute(container, new CommandArguments(controller, "/kick user1"),
                new ChannelCommandContext(null, KickReason.INFO, channel));

        verify(eventbus).publishAsync(errorEventCaptor.capture());
        assertTrue(errorEventCaptor.getValue().getMessage().matches(".*user1"));
    }

    @Test
    public void testWithReason() {
        command.execute(container, new CommandArguments(controller, "/kick user reason here"),
                new ChannelCommandContext(null, KickReason.INFO, channel));

        verify(channel).kick(groupChatUser, Optional.of("reason here"));
    }

    @Test
    public void testWithoutReason() {
        command.execute(container, new CommandArguments(controller, "/kick user"),
                new ChannelCommandContext(null, KickReason.INFO, channel));
        verify(channel).kick(groupChatUser, Optional.empty());
    }
}
