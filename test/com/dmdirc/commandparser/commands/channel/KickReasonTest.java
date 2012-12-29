/*
 * Copyright (c) 2006-2012 DMDirc Developers
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
import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.ChannelCommandContext;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.parser.irc.IRCChannelClientInfo;
import com.dmdirc.parser.irc.IRCChannelInfo;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class KickReasonTest {

    private CommandController controller;
    private KickReason command;

    @Before
    public void setUp() throws Exception {
        controller = mock(CommandController.class);
        when(controller.getCommandChar()).thenReturn('/');
        command = new KickReason(controller);
    }

    @Test
    public void testUsage() {
        final FrameContainer tiw = mock(FrameContainer.class);
        final Channel channel = mock(Channel.class);
        command.execute(tiw, new CommandArguments(controller, "/kick"),
                new ChannelCommandContext(null, KickReason.INFO, channel));

        verify(tiw).addLine(eq("commandUsage"), anyChar(), anyString(), anyString());
    }

    @Test
    public void testUnknown() {
        final FrameContainer tiw = mock(FrameContainer.class);
        final IRCChannelInfo channelInfo = mock(IRCChannelInfo.class);
        final Channel channel = mock(Channel.class);

        when(channel.getChannelInfo()).thenReturn(channelInfo);
        when(channelInfo.getChannelClient(anyString())).thenReturn(null);

        command.execute(tiw, new CommandArguments(controller, "/kick user1"),
                new ChannelCommandContext(null, KickReason.INFO, channel));

        verify(tiw).addLine(eq("commandError"), matches(".*user1"));
    }

    @Test
    public void testWithReason() {
        final FrameContainer tiw = mock(FrameContainer.class);
        final IRCChannelInfo channelInfo = mock(IRCChannelInfo.class);
        final Channel channel = mock(Channel.class);
        final IRCChannelClientInfo cci = mock(IRCChannelClientInfo.class);

        when(channel.getChannelInfo()).thenReturn(channelInfo);
        when(channelInfo.getChannelClient("user1")).thenReturn(cci);

        command.execute(tiw, new CommandArguments(controller, "/kick user1 reason here"),
                new ChannelCommandContext(null, KickReason.INFO, channel));

        verify(cci).kick("reason here");
    }

    @Test
    public void testWithoutReason() {
        final FrameContainer tiw = mock(FrameContainer.class);
        final ConfigManager manager = mock(ConfigManager.class);
        final IRCChannelInfo channelInfo = mock(IRCChannelInfo.class);
        final Channel channel = mock(Channel.class);
        final IRCChannelClientInfo cci = mock(IRCChannelClientInfo.class);

        when(tiw.getConfigManager()).thenReturn(manager);
        when(channel.getChannelInfo()).thenReturn(channelInfo);
        when(channelInfo.getChannelClient("user1")).thenReturn(cci);
        when(manager.getOption("general", "kickmessage")).thenReturn("reason here");

        command.execute(tiw, new CommandArguments(controller, "/kick user1"),
                new ChannelCommandContext(null, KickReason.INFO, channel));

        verify(cci).kick("reason here");
    }
}
