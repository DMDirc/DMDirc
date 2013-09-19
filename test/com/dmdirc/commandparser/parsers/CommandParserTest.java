/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

import com.dmdirc.TestMain;
import com.dmdirc.Main;
import com.dmdirc.ServerManager;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.global.Echo;
import com.dmdirc.config.ConfigBinder;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.harness.TestCommandParser;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CommandParserTest {

    @Mock private ServerManager serverManager;
    @Mock private ConfigManager cm;
    private CommandManager commands;

    @Before
    public void setup() throws InvalidIdentityFileException {
        MockitoAnnotations.initMocks(this);
        when(cm.getOptionChar("general", "silencechar")).thenReturn('.');
        when(cm.getOptionInt("general", "commandhistory")).thenReturn(10);
        when(cm.getOptionChar("general", "commandchar")).thenReturn('/');
        final ConfigBinder binder = new ConfigBinder(cm);
        when(cm.getBinder()).thenReturn(binder);
        commands = new CommandManager(serverManager);
        commands.initialise(cm);
        commands.registerCommand(new Echo(commands), Echo.INFO);
    }

    @Test
    public void testBasicCommand() {
        final TestCommandParser tcp = new TestCommandParser(cm, commands);
        tcp.parseCommand(null, "/echo this is a test");

        assertNull(tcp.nonCommandLine);
        assertNull(tcp.invalidCommand);
        assertNotNull(tcp.executedCommand);
        assertFalse(tcp.wasSilent);
        assertTrue(tcp.executedCommand instanceof Echo);
    }

    @Test
    public void testBasicNoArgs() {
        final TestCommandParser tcp = new TestCommandParser(cm, commands);
        tcp.parseCommand(null, "/echo");

        assertNull(tcp.nonCommandLine);
        assertNull(tcp.invalidCommand);
        assertNotNull(tcp.executedCommand);
        assertFalse(tcp.wasSilent);
        assertTrue(tcp.executedCommand instanceof Echo);
    }

    @Test
    public void testSilentNoArgs() {
        final TestCommandParser tcp = new TestCommandParser(cm, commands);
        tcp.parseCommand(null, "/.echo");

        assertNull(tcp.nonCommandLine);
        assertNull(tcp.invalidCommand);
        assertNotNull(tcp.executedCommand);
        assertTrue(tcp.wasSilent);
        assertTrue(tcp.executedCommand instanceof Echo);
    }

    @Test
    public void testSilentCommand() {
        final TestCommandParser tcp = new TestCommandParser(cm, commands);
        tcp.parseCommand(null, "/.echo this is a test");

        assertNull(tcp.nonCommandLine);
        assertNull(tcp.invalidCommand);
        assertNotNull(tcp.executedCommand);
        assertTrue(tcp.wasSilent);
        assertTrue(tcp.executedCommand instanceof Echo);
    }

    @Test
    public void testNonExistantCommand() {
        final TestCommandParser tcp = new TestCommandParser(cm, commands);
        tcp.parseCommand(null, "/foobar moo bar");

        assertNull(tcp.nonCommandLine);
        assertEquals("foobar", tcp.invalidCommand);
        assertNotNull(tcp.invalidCommand);
        assertNull(tcp.executedCommand);
        assertFalse(tcp.wasSilent);
    }

    @Test
    public void testEmptyCommand() {
        final TestCommandParser tcp = new TestCommandParser(cm, commands);
        tcp.parseCommand(null, "/ moo bar");

        assertNull(tcp.nonCommandLine);
        assertEquals("", tcp.invalidCommand);
        assertNotNull(tcp.invalidCommand);
        assertNull(tcp.executedCommand);
        assertFalse(tcp.wasSilent);
    }

    @Test
    public void testEmptySilentCommand() {
        final TestCommandParser tcp = new TestCommandParser(cm, commands);
        tcp.parseCommand(null, "/. moo bar");

        assertNull(tcp.nonCommandLine);
        assertEquals("", tcp.invalidCommand);
        assertNotNull(tcp.invalidCommand);
        assertNull(tcp.executedCommand);
        assertFalse(tcp.wasSilent);
    }

    @Test
    public void testNonCommand() {
        final TestCommandParser tcp = new TestCommandParser(cm, commands);
        tcp.parseCommand(null, "Foobar baz");

        assertNotNull(tcp.nonCommandLine);
        assertEquals("Foobar baz", tcp.nonCommandLine);
        assertNull(tcp.invalidCommand);
        assertNull(tcp.executedCommand);
        assertFalse(tcp.wasSilent);
    }

    @Test
    public void testCommandHistory() {
        final TestCommandParser tcp = new TestCommandParser(cm, commands);
        tcp.parseCommand(null, "/echo this is a test");

        final long time1 = tcp.getCommandTime("echo this is a test");
        assertTrue(time1 > 0);

        tcp.parseCommand( null, "/echo this is a test");
        final long time2 = tcp.getCommandTime("echo this is a test");
        assertTrue(time2 > 0);
        assertTrue(time2 >= time1);

        assertEquals(0L, tcp.getCommandTime("echo"));
    }

}
