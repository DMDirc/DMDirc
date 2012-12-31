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
import com.dmdirc.Server;
import com.dmdirc.TestMain;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.ChannelCommandContext;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.parser.irc.IRCChannelInfo;
import com.dmdirc.parser.irc.IRCParser;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class NamesTest {

    private Names command;
    private IRCChannelInfo channelinfo;
    private Channel channel;
    private Server server;
    private IRCParser parser;

    @Before
    public void setUp() throws InvalidIdentityFileException {
        TestMain.getTestMain();

        parser = mock(IRCParser.class);
        server = mock(Server.class);
        channel = mock(Channel.class);
        channelinfo = mock(IRCChannelInfo.class);

        when(channel.getServer()).thenReturn(server);
        when(server.getParser()).thenReturn(parser);
        when(channel.getChannelInfo()).thenReturn(channelinfo);
        when(channelinfo.getName()).thenReturn("#chan");

        command = new Names();
    }

    @Test
    public void testNormal() {
        command.execute(null, new CommandArguments("/names"),
                new ChannelCommandContext(null, Names.INFO, channel));

        verify(parser).sendRawMessage("NAMES #chan");
    }

    @Test
    public void testExternal() {
        command.execute(null, server, "#chan", false, new CommandArguments("/names #chan"));

        verify(parser).sendRawMessage("NAMES #chan");
    }

}