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

package com.dmdirc.commandparser.commands.channel;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.ChannelCommandContext;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.Parser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ModeTest {

    @Mock private FrameContainer origin;
    @Mock private CommandController controller;
    @Mock private ChannelInfo channelinfo;
    @Mock private Channel channel;
    @Mock private Server server;
    @Mock private Parser parser;
    private Mode command;

    @Before
    public void setUp() throws InvalidIdentityFileException {
        when(channel.getConnection()).thenReturn(server);
        when(server.getParser()).thenReturn(parser);
        when(channel.getChannelInfo()).thenReturn(channelinfo);
        when(channelinfo.getModes()).thenReturn("my mode string!");
        when(channelinfo.toString()).thenReturn("#chan");

        command = new Mode(controller);
    }

    @Test
    public void testWithoutArgs() {
        command.execute(origin, new CommandArguments(controller, "/mode"),
                new ChannelCommandContext(null, Mode.INFO, channel));

        verify(origin).addLine("channelModeDiscovered", "my mode string!", channelinfo);
    }

    @Test
    public void testWithArgs() {
        command.execute(origin, new CommandArguments(controller, "/mode +hello -bye"),
                new ChannelCommandContext(null, Mode.INFO, channel));

        verify(parser).sendRawMessage("MODE #chan +hello -bye");
    }

    @Test
    public void testExternalWithArgs() {
        command.execute(origin, server, "#chan", false,
                new CommandArguments(controller, "/mode +hello -bye"));

        verify(parser).sendRawMessage("MODE #chan +hello -bye");
    }

    @Test
    public void testExternalWithoutArgs() {
        command.execute(origin, server, "#chan", false,
                new CommandArguments(controller, "/mode"));

        verify(parser).sendRawMessage("MODE #chan");
    }

}
