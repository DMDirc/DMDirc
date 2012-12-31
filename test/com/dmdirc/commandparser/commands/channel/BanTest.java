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
import com.dmdirc.TestMain;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.ChannelCommandContext;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class BanTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        TestMain.getTestMain();
    }

    private final Ban command = new Ban();

    @Test
    public void testUsage() {
        final FrameContainer tiw = mock(FrameContainer.class);
        final Channel channel = mock(Channel.class);
        command.execute(tiw, new CommandArguments("/ban"),
                new ChannelCommandContext(null, Ban.INFO, channel));

        verify(tiw).addLine(eq("commandUsage"), anyChar(), anyString(), anyString());
    }

    /** Tests that the ban command uses the correct hostname if given a user. */
    @Test
    public void testKnownUser() {
        final FrameContainer container = mock(FrameContainer.class);
        final ChannelInfo channelInfo = mock(ChannelInfo.class);
        final ChannelClientInfo ccInfo = mock(ChannelClientInfo.class);
        final ClientInfo clientInfo = mock(ClientInfo.class);
        final Channel channel = mock(Channel.class);

        when(channel.getChannelInfo()).thenReturn(channelInfo);
        when(channelInfo.getChannelClient("user")).thenReturn(ccInfo);
        when(ccInfo.getClient()).thenReturn(clientInfo);
        when(clientInfo.getHostname()).thenReturn("my.host.name");

        command.execute(container, new CommandArguments("/ban user"),
                new ChannelCommandContext(null, Ban.INFO, channel));

        verify(channelInfo).alterMode(true, 'b', "*!*@my.host.name");
        verify(channelInfo).flushModes();
    }

    /** Tests that the ban command works if given a mask not a username. */
    @Test
    public void testHostmask() {
        final FrameContainer container = mock(FrameContainer.class);
        final ChannelInfo channelInfo = mock(ChannelInfo.class);
        final Channel channel = mock(Channel.class);

        when(channel.getChannelInfo()).thenReturn(channelInfo);

        command.execute(container, new CommandArguments("/ban *!*@my.host.name"),
                new ChannelCommandContext(null, Ban.INFO, channel));

        verify(channelInfo).alterMode(true, 'b', "*!*@my.host.name");
        verify(channelInfo).flushModes();
    }
}
