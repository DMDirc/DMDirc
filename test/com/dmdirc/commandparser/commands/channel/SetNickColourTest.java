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
import com.dmdirc.DMDircMBassador;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.ChannelCommandContext;
import com.dmdirc.events.CommandErrorEvent;
import com.dmdirc.events.DisplayProperty;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.GroupChatUser;
import com.dmdirc.interfaces.User;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.messages.ColourManagerFactory;
import com.dmdirc.util.colours.Colour;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SetNickColourTest {

    @Mock private Connection connection;
    @Mock private User user1;
    @Mock private User user2;
    @Mock private GroupChatUser groupChatUser;
    @Mock private Channel channel;
    @Mock private ColourManagerFactory colourManagerFactory;
    @Mock private ColourManager colourManager;
    @Mock private CommandController controller;
    @Mock private WindowModel tiw;
    @Mock private DMDircMBassador eventbus;
    @Captor private ArgumentCaptor<CommandErrorEvent> errorEventCaptor;
    private SetNickColour command;

    @Before
    public void setUp() {
        command = new SetNickColour(controller, colourManagerFactory);
        when(controller.getCommandChar()).thenReturn('/');
        when(controller.getSilenceChar()).thenReturn('.');
        when(channel.getConnection()).thenReturn(Optional.of(connection));
        when(connection.getUser("moo")).thenReturn(user1);
        when(connection.getUser("foo")).thenReturn(user2);
        when(channel.getUser(user1)).thenReturn(Optional.of(groupChatUser));
        when(channel.getUser(user2)).thenReturn(Optional.empty());
        when(colourManagerFactory.getColourManager(any())).thenReturn(colourManager);
        when(colourManager.getColourFromString(eq("4"), any())).thenReturn(Colour.RED);
        when(tiw.getEventBus()).thenReturn(eventbus);
    }

    @Test
    public void testUsageNoArgs() {
        command.execute(tiw, new CommandArguments(controller, "/foo"),
                new ChannelCommandContext(null, SetNickColour.INFO, channel));
        verify(eventbus).publishAsync(isA(CommandErrorEvent.class));
    }

    @Test
    public void testUsageNicknameValid() {
        command.execute(tiw, new CommandArguments(controller, "/foo moo"),
                new ChannelCommandContext(null, SetNickColour.INFO, channel));
        verify(channel).refreshClients();
        verify(groupChatUser).removeDisplayProperty(DisplayProperty.FOREGROUND_COLOUR);
    }

    @Test
    public void testUsageNicknameInvalid() {
        command.execute(tiw, new CommandArguments(controller, "/foo foo"),
                new ChannelCommandContext(null, SetNickColour.INFO, channel));
        verify(channel, never()).refreshClients();
        verify(groupChatUser, never()).removeDisplayProperty(DisplayProperty.FOREGROUND_COLOUR);
        verify(eventbus).publishAsync(errorEventCaptor.capture());
        assertEquals("No such nickname (foo)!", errorEventCaptor.getValue().getMessage());
    }

    @Test
    public void testUsageInvalidColour() {
        command.execute(tiw, new CommandArguments(controller, "/foo moo omg"),
                new ChannelCommandContext(null, SetNickColour.INFO, channel));
        verify(eventbus).publishAsync(errorEventCaptor.capture());
        assertEquals("Invalid colour specified (omg).", errorEventCaptor.getValue().getMessage());
    }

    @Test
    public void testUsageValidColour() {
        command.execute(tiw, new CommandArguments(controller, "/foo moo 4"),
                new ChannelCommandContext(null, SetNickColour.INFO, channel));
        verify(channel).refreshClients();
        verify(groupChatUser).setDisplayProperty(DisplayProperty.FOREGROUND_COLOUR, Colour.RED);
    }

}