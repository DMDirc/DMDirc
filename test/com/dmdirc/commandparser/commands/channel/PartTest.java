/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.InvalidIdentityFileException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class PartTest {

    private final Part command = new Part();
    private Channel channel;
    private FrameContainer<?> origin;
    private ConfigManager manager;

    @BeforeClass
    public static void setUpClass() throws InvalidIdentityFileException {
        IdentityManager.load();
    }

    @Before
    public void setUp() throws InvalidIdentityFileException {
        IdentityManager.load();
        
        channel = mock(Channel.class);
        origin = mock(FrameContainer.class);
        manager = mock(ConfigManager.class);
        
        when(origin.getConfigManager()).thenReturn(manager);
        when(manager.getOption("general", "partmessage")).thenReturn("config part message");
    }

    @Test
    public void testWithoutArgs() {
        command.execute(origin, new CommandArguments("/part"),
                new ChannelCommandContext(null, command, channel));

        verify(channel).part("config part message");
        verify(channel).close();
    }

    @Test
    public void testWithArgs() {
        command.execute(origin, new CommandArguments("/part custom part"),
                new ChannelCommandContext(null, command, channel));

        verify(channel).part("custom part");
        verify(channel).close();
    }

}