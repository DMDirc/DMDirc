/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.config.IdentityManager;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.parser.irc.ChannelClientInfo;
import com.dmdirc.parser.irc.ChannelInfo;
import com.dmdirc.ui.interfaces.InputWindow;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class KickReasonTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        IdentityManager.load();
    }
    
    private final KickReason command = new KickReason();

    @Test
    public void testUsage() {
        final InputWindow tiw = mock(InputWindow.class);
        command.execute(tiw, null, null, false, new CommandArguments("/kick"));
        
        verify(tiw).addLine(eq("commandUsage"), anyChar(), anyString(), anyString());
    }

    @Test
    public void testUnknown() {
        final InputWindow tiw = mock(InputWindow.class);
        final ChannelInfo channelInfo = mock(ChannelInfo.class);
        final Channel channel = mock(Channel.class);

        when(channel.getChannelInfo()).thenReturn(channelInfo);
        when(channelInfo.getUser(anyString())).thenReturn(null);

        command.execute(tiw, null, channel, false, new CommandArguments("/kick user1"));

        verify(tiw).addLine(eq("commandError"), matches(".*user1"));
    }

    @Test
    public void testWithReason() {
        final InputWindow tiw = mock(InputWindow.class);
        final ChannelInfo channelInfo = mock(ChannelInfo.class);
        final Channel channel = mock(Channel.class);
        final ChannelClientInfo cci = mock(ChannelClientInfo.class);

        when(channel.getChannelInfo()).thenReturn(channelInfo);
        when(channelInfo.getUser("user1")).thenReturn(cci);

        command.execute(tiw, null, channel, false, new CommandArguments("/kick user1 reason here"));

        verify(cci).kick("reason here");
    }

    @Test
    public void testWithoutReason() {
        final InputWindow tiw = mock(InputWindow.class);
        final ConfigManager manager = mock(ConfigManager.class);
        final ChannelInfo channelInfo = mock(ChannelInfo.class);
        final Channel channel = mock(Channel.class);
        final ChannelClientInfo cci = mock(ChannelClientInfo.class);

        when(tiw.getConfigManager()).thenReturn(manager);
        when(channel.getChannelInfo()).thenReturn(channelInfo);
        when(channelInfo.getUser("user1")).thenReturn(cci);
        when(manager.getOption("general", "kickmessage")).thenReturn("reason here");

        command.execute(tiw, null, channel, false, new CommandArguments("/kick user1"));

        verify(cci).kick("reason here");
    }
}