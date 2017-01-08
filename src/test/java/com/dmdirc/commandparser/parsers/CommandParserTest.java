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

package com.dmdirc.commandparser.parsers;

import com.dmdirc.Channel;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.harness.TestCommandParser;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.GroupChatManager;
import com.dmdirc.interfaces.InputModel;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.interfaces.config.AggregateConfigProvider;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommandParserTest {

    @Mock private AggregateConfigProvider configProvider;
    @Mock private CommandController commandController;
    @Mock private CommandInfo commandInfo;
    @Mock private CommandInfo channelCommandInfo;
    @Mock private Command command;
    @Mock private Command channelCommand;
    @Mock private WindowModel container;
    @Mock private InputModel inputModel;
    @Mock private Channel channel;
    @Mock private Connection connection;
    @Mock private GroupChatManager groupChatManager;
    @Mock private EventBus eventBus;
    private TestCommandParser commandParser;
    private TestCommandParser channelCommandParser;

    @Before
    public void setup() {
        when(commandController.getCommandChar()).thenReturn('/');
        when(commandController.getSilenceChar()).thenReturn('.');
        when(commandController.isChannelCommand("channel")).thenReturn(true);

        when(commandInfo.getName()).thenReturn("command");
        when(channelCommandInfo.getName()).thenReturn("channel");

        when(configProvider.getOptionInt("general", "commandhistory")).thenReturn(10);

        when(container.getConnection()).thenReturn(Optional.of(connection));
        when(connection.getGroupChatManager()).thenReturn(groupChatManager);
        when(groupChatManager.isValidChannelName("#channel1")).thenReturn(true);
        when(groupChatManager.getChannel("#channel1")).thenReturn(Optional.of(channel));

        commandParser = new TestCommandParser(configProvider, commandController, eventBus);
        commandParser.registerCommand(command, commandInfo);
        commandParser.registerCommand(channelCommand, channelCommandInfo);

        channelCommandParser = new TestCommandParser(configProvider, commandController, eventBus);
        channelCommandParser.registerCommand(channelCommand, channelCommandInfo);

        when(channel.getWindowModel()).thenReturn(channel);
        when(channel.getInputModel()).thenReturn(Optional.of(inputModel));
        when(inputModel.getCommandParser()).thenReturn(channelCommandParser);
    }

    @Test
    public void testParseCommandWithArguments() {
        commandParser.parseCommand(container, "/command this is a test");

        assertNull(commandParser.nonCommandLine);
        assertNull(commandParser.invalidCommand);
        assertFalse(commandParser.wasSilent);
        assertSame(command, commandParser.executedCommand);
        assertEquals("this is a test", commandParser.commandArgs.getArgumentsAsString());
    }

    @Test
    public void testParseCommandWithoutArguments() {
        commandParser.parseCommand(container, "/command");

        assertNull(commandParser.nonCommandLine);
        assertNull(commandParser.invalidCommand);
        assertFalse(commandParser.wasSilent);
        assertSame(command, commandParser.executedCommand);
        assertEquals("", commandParser.commandArgs.getArgumentsAsString());
    }

    @Test
    public void testParseSilentCommandWithoutArguments() {
        commandParser.parseCommand(container, "/.command");

        assertNull(commandParser.nonCommandLine);
        assertNull(commandParser.invalidCommand);
        assertTrue(commandParser.wasSilent);
        assertSame(command, commandParser.executedCommand);
        assertEquals("", commandParser.commandArgs.getArgumentsAsString());
    }

    @Test
    public void testParseSilentCommandWithArguments() {
        commandParser.parseCommand(container, "/.command this is a test");

        assertNull(commandParser.nonCommandLine);
        assertNull(commandParser.invalidCommand);
        assertTrue(commandParser.wasSilent);
        assertSame(command, commandParser.executedCommand);
        assertEquals("this is a test", commandParser.commandArgs.getArgumentsAsString());
    }

    @Test
    public void testParseUnknownCommand() {
        commandParser.parseCommand(container, "/foobar moo bar");

        assertNull(commandParser.nonCommandLine);
        assertEquals("foobar", commandParser.invalidCommand);
        assertNull(commandParser.executedCommand);
        assertFalse(commandParser.wasSilent);
    }

    @Test
    public void testParseEmptyCommand() {
        commandParser.parseCommand(container, "/ moo bar");

        assertNull(commandParser.nonCommandLine);
        assertEquals("", commandParser.invalidCommand);
        assertNull(commandParser.executedCommand);
        assertFalse(commandParser.wasSilent);
    }

    @Test
    public void testParseEmptySilenceCommand() {
        commandParser.parseCommand(container, "/. moo bar");

        assertNull(commandParser.nonCommandLine);
        assertEquals("", commandParser.invalidCommand);
        assertNull(commandParser.executedCommand);
        assertFalse(commandParser.wasSilent);
    }

    @Test
    public void testParseNonCommand() {
        commandParser.parseCommand(container, "Foobar baz");

        assertEquals("Foobar baz", commandParser.nonCommandLine);
        assertNull(commandParser.invalidCommand);
        assertNull(commandParser.executedCommand);
        assertFalse(commandParser.wasSilent);
    }

    @Test
    public void testGetCommandTime() {
        commandParser.parseCommand(container, "/command this is a test");

        final long time1 = commandParser.getCommandTime("command this is a test");
        assertTrue(time1 > 0);

        commandParser.parseCommand(container, "/command this is a test");
        final long time2 = commandParser.getCommandTime("command this is a test");
        assertTrue(time2 > 0);
        assertTrue(time2 >= time1);

        assertEquals(0L, commandParser.getCommandTime("command"));
    }

    @Test
    public void testParseChannelCommandWithArguments() {
        when(container.getConnection()).thenReturn(Optional.of(connection));
        commandParser.parseCommand(container, "/channel #channel1 this is a test");

        assertNull(channelCommandParser.nonCommandLine);
        assertNull(channelCommandParser.invalidCommand);
        assertFalse(channelCommandParser.wasSilent);
        assertSame(channelCommand, channelCommandParser.executedCommand);
        assertEquals("this is a test", channelCommandParser.commandArgs.getArgumentsAsString());
    }

    @Test
    public void testParseChannelCommandWithoutArguments() {
        when(container.getConnection()).thenReturn(Optional.of(connection));
        commandParser.parseCommand(container, "/channel #channel1");

        assertNull(channelCommandParser.nonCommandLine);
        assertNull(channelCommandParser.invalidCommand);
        assertFalse(channelCommandParser.wasSilent);
        assertSame(channelCommand, channelCommandParser.executedCommand);
        assertEquals("", channelCommandParser.commandArgs.getArgumentsAsString());
    }

    @Test
    public void testParseSilencedChannelCommandWithArguments() {
        when(container.getConnection()).thenReturn(Optional.of(connection));
        commandParser.parseCommand(container, "/.channel #channel1 this is a test");

        assertNull(channelCommandParser.nonCommandLine);
        assertNull(channelCommandParser.invalidCommand);
        assertTrue(channelCommandParser.wasSilent);
        assertSame(channelCommand, channelCommandParser.executedCommand);
        assertEquals("this is a test", channelCommandParser.commandArgs.getArgumentsAsString());
    }

    @Test
    public void testParseSilencedChannelCommandWithoutArguments() {
        when(container.getConnection()).thenReturn(Optional.of(connection));
        commandParser.parseCommand(container, "/.channel #channel1");

        assertNull(channelCommandParser.nonCommandLine);
        assertNull(channelCommandParser.invalidCommand);
        assertTrue(channelCommandParser.wasSilent);
        assertSame(channelCommand, channelCommandParser.executedCommand);
        assertEquals("", channelCommandParser.commandArgs.getArgumentsAsString());
    }

    @Test
    public void testParseUnregisterCommand() {
        commandParser.unregisterCommand(commandInfo);
        commandParser.parseCommand(container, "/command test 123");

        assertNull(commandParser.nonCommandLine);
        assertEquals("command", commandParser.invalidCommand);
        assertNull(commandParser.executedCommand);
        assertFalse(commandParser.wasSilent);
    }

    @Test
    public void testGetCommands() {
        assertEquals(2, commandParser.getCommands().size());
        assertTrue(commandParser.getCommands().containsKey("command"));
        assertTrue(commandParser.getCommands().containsKey("channel"));

        commandParser.unregisterCommand(commandInfo);
        assertEquals(1, commandParser.getCommands().size());
        assertFalse(commandParser.getCommands().containsKey("command"));
        assertTrue(commandParser.getCommands().containsKey("channel"));
    }

}
