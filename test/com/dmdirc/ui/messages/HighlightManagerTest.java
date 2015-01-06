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
import com.dmdirc.DMDircMBassador;
import com.dmdirc.config.profiles.Profile;
import com.dmdirc.events.ChannelHighlightEvent;
import com.dmdirc.events.ChannelMessageEvent;
import com.dmdirc.events.ServerConnectedEvent;
import com.dmdirc.events.ServerNickChangeEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.GroupChatUser;
import com.dmdirc.interfaces.User;
import com.dmdirc.interfaces.config.AggregateConfigProvider;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HighlightManagerTest {

    @Mock private Connection connection;
    @Mock private User user;
    @Mock private Profile profile;

    @Mock private Channel channel;
    @Mock private GroupChatUser channelUser;
    @Mock private DMDircMBassador eventBus;

    @Mock private ColourManager colourManager;
    @Mock private AggregateConfigProvider configProvider;
    private HighlightManager manager;

    @Before
    public void setup() {
        when(connection.getLocalUser()).thenReturn(Optional.of(user));
        when(connection.getProfile()).thenReturn(profile);

        when(channel.getEventBus()).thenReturn(eventBus);

        manager = new HighlightManager(configProvider, colourManager);
    }

    @Test
    public void testNicknameInChannel() {
        when(user.getNickname()).thenReturn("nickName");
        when(profile.getHighlights()).thenReturn(Collections.<String>emptyList());

        final ChannelMessageEvent event = new ChannelMessageEvent(channel, channelUser,
                "Hi, nickName!");

        manager.handleConnected(new ServerConnectedEvent(connection));
        manager.handleChannelMessage(event);

        final ArgumentCaptor<ChannelHighlightEvent> captor =
                ArgumentCaptor.forClass(ChannelHighlightEvent.class);

        verify(eventBus).publishAsync(captor.capture());
        assertSame(event, captor.getValue().getCause());
    }

    @Test
    public void testNewNicknameInChannelAfterNickChange() {
        when(user.getNickname()).thenReturn("nickName");
        when(profile.getHighlights()).thenReturn(Collections.<String>emptyList());

        final ChannelMessageEvent event = new ChannelMessageEvent(channel, channelUser,
                "Hi, newName!");

        manager.handleConnected(new ServerConnectedEvent(connection));
        manager.handleNickChange(new ServerNickChangeEvent(connection, "nickName", "newName"));
        manager.handleChannelMessage(event);

        final ArgumentCaptor<ChannelHighlightEvent> captor =
                ArgumentCaptor.forClass(ChannelHighlightEvent.class);

        verify(eventBus).publishAsync(captor.capture());
        assertSame(event, captor.getValue().getCause());
    }

    @Test
    public void testOldNicknameInChannelAfterNickChange() {
        when(user.getNickname()).thenReturn("nickName");
        when(profile.getHighlights()).thenReturn(Collections.<String>emptyList());

        final ChannelMessageEvent event = new ChannelMessageEvent(channel, channelUser,
                "Hi, nickName!");

        manager.handleConnected(new ServerConnectedEvent(connection));
        manager.handleNickChange(new ServerNickChangeEvent(connection, "nickName", "newName"));
        manager.handleChannelMessage(event);

        verify(eventBus, never()).publishAsync(any());
    }

    @Test
    public void testCustomHighlightInChannel() {
        when(user.getNickname()).thenReturn("nickName");
        when(profile.getHighlights()).thenReturn(Lists.newArrayList("dmdirc"));

        final ChannelMessageEvent event = new ChannelMessageEvent(channel, channelUser,
                "DMDirc is great.");

        manager.handleConnected(new ServerConnectedEvent(connection));
        manager.handleChannelMessage(event);

        final ArgumentCaptor<ChannelHighlightEvent> captor =
                ArgumentCaptor.forClass(ChannelHighlightEvent.class);

        verify(eventBus).publishAsync(captor.capture());
        assertSame(event, captor.getValue().getCause());
    }

    @Test
    public void testMultipleCustomHighlightInChannel() {
        when(user.getNickname()).thenReturn("nickName");
        when(profile.getHighlights()).thenReturn(
                Lists.newArrayList("dmdirc", "is", "great", "e"));

        final ChannelMessageEvent event = new ChannelMessageEvent(channel, channelUser,
                "DMDirc is great.");

        manager.handleConnected(new ServerConnectedEvent(connection));
        manager.handleChannelMessage(event);

        final ArgumentCaptor<ChannelHighlightEvent> captor =
                ArgumentCaptor.forClass(ChannelHighlightEvent.class);

        verify(eventBus, only()).publishAsync(captor.capture());
        assertSame(event, captor.getValue().getCause());
    }

}