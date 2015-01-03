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
import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.ChannelCommandContext;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.config.AggregateConfigProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PartTest {

    @Mock private Channel channel;
    @Mock private FrameContainer origin;
    @Mock private FrameContainer windowModel;
    @Mock private AggregateConfigProvider manager;
    @Mock private CommandController controller;
    private Part command;

    @Before
    public void setUp() throws InvalidIdentityFileException {
        when(channel.getWindowModel()).thenReturn(windowModel);
        when(origin.getConfigManager()).thenReturn(manager);
        when(manager.getOption("general", "partmessage")).thenReturn("config part message");

        command = new Part(controller);
    }

    @Test
    public void testWithoutArgs() {
        command.execute(origin, new CommandArguments(controller, "/part"),
                new ChannelCommandContext(null, Part.INFO, channel));

        verify(channel).part("config part message");
        verify(windowModel).close();
    }

    @Test
    public void testWithArgs() {
        command.execute(origin, new CommandArguments(controller, "/part custom part"),
                new ChannelCommandContext(null, Part.INFO, channel));

        verify(channel).part("custom part");
        verify(windowModel).close();
    }

}