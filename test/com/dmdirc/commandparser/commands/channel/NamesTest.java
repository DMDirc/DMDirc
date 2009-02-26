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
import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.parser.irc.ChannelInfo;
import com.dmdirc.parser.irc.IRCParser;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class NamesTest {

    private final Names command = new Names();
    private ChannelInfo channelinfo;
    private Channel channel;
    private Server server;
    private IRCParser parser;

    @BeforeClass
    public static void setUpClass() {
        IdentityManager.load();
    }

    @Before
    public void setUp() {
        IdentityManager.load();
        
        parser = mock(IRCParser.class);
        server = mock(Server.class);
        channel = mock(Channel.class);
        channelinfo = mock(ChannelInfo.class);

        when(server.getParser()).thenReturn(parser);
        when(channel.getChannelInfo()).thenReturn(channelinfo);
        when(channelinfo.getName()).thenReturn("#chan");
    }

    @Test
    public void testNormal() {
        command.execute(null, server, channel, false, new CommandArguments("/names"));

        verify(parser).sendLine("NAMES #chan");
    }

    @Test
    public void testExternal() {
        command.execute(null, server, "#chan", false, new CommandArguments("/names #chan"));

        verify(parser).sendLine("NAMES #chan");
    }

}