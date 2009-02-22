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
package com.dmdirc.commandparser.parsers;

import com.dmdirc.harness.TestCommandParser;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.global.Echo;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.interfaces.InputWindow;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class CommandParserTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        IdentityManager.load();
        CommandManager.initCommands();
    }

    @Test
    public void testBasicCommand() {
        final TestCommandParser tcp = new TestCommandParser();
        tcp.parseCommand(null, "/echo this is a test");

        assertNull(tcp.nonCommandLine);
        assertNull(tcp.invalidCommand);
        assertNotNull(tcp.executedCommand);
        assertFalse(tcp.wasSilent);
        assertTrue(tcp.executedCommand instanceof Echo);
    }

    @Test
    public void testBasicNoArgs() {
        final TestCommandParser tcp = new TestCommandParser();
        tcp.parseCommand(null, "/echo");

        assertNull(tcp.nonCommandLine);
        assertNull(tcp.invalidCommand);
        assertNotNull(tcp.executedCommand);
        assertFalse(tcp.wasSilent);
        assertTrue(tcp.executedCommand instanceof Echo);
    }

    @Test
    public void testSilentNoArgs() {
        final TestCommandParser tcp = new TestCommandParser();
        tcp.parseCommand(null, "/.echo");

        assertNull(tcp.nonCommandLine);
        assertNull(tcp.invalidCommand);
        assertNotNull(tcp.executedCommand);
        assertTrue(tcp.wasSilent);
        assertTrue(tcp.executedCommand instanceof Echo);
    }

    @Test
    public void testSilentCommand() {
        final TestCommandParser tcp = new TestCommandParser();
        tcp.parseCommand(null, "/.echo this is a test");

        assertNull(tcp.nonCommandLine);
        assertNull(tcp.invalidCommand);
        assertNotNull(tcp.executedCommand);
        assertTrue(tcp.wasSilent);
        assertTrue(tcp.executedCommand instanceof Echo);
    }

    @Test
    public void testNonExistantCommand() {
        final TestCommandParser tcp = new TestCommandParser();
        tcp.parseCommand(null, "/foobar moo bar");

        assertNull(tcp.nonCommandLine);
        assertEquals("foobar", tcp.invalidCommand);
        assertNotNull(tcp.invalidCommand);
        assertNull(tcp.executedCommand);
        assertFalse(tcp.wasSilent);
    }

    @Test
    public void testEmptyCommand() {
        final TestCommandParser tcp = new TestCommandParser();
        tcp.parseCommand(null, "/ moo bar");

        assertNull(tcp.nonCommandLine);
        assertEquals("", tcp.invalidCommand);
        assertNotNull(tcp.invalidCommand);
        assertNull(tcp.executedCommand);
        assertFalse(tcp.wasSilent);
    }
    
    @Test
    public void testEmptySilentCommand() {
        final TestCommandParser tcp = new TestCommandParser();
        tcp.parseCommand(null, "/. moo bar");

        assertNull(tcp.nonCommandLine);
        assertEquals("", tcp.invalidCommand);
        assertNotNull(tcp.invalidCommand);
        assertNull(tcp.executedCommand);
        assertFalse(tcp.wasSilent);
    }
    
    @Test
    public void testNonCommand() {
        final TestCommandParser tcp = new TestCommandParser();
        tcp.parseCommand(null, "Foobar baz");

        assertNotNull(tcp.nonCommandLine);
        assertEquals("Foobar baz", tcp.nonCommandLine);
        assertNull(tcp.invalidCommand);
        assertNull(tcp.executedCommand);
        assertFalse(tcp.wasSilent);
    }
    
    @Test
    public void testCommandHistory() {
        final TestCommandParser tcp = new TestCommandParser();
        tcp.parseCommand(null, "/echo this is a test");

        final long time1 = tcp.getCommandTime("echo this is a test");
        assertTrue(time1 > 0);
        
        tcp.parseCommand(null, "/echo this is a test");
        final long time2 = tcp.getCommandTime("echo this is a test");
        assertTrue(time2 > 0);
        assertTrue(time2 >= time1);
        
        assertEquals(0l, tcp.getCommandTime("echo"));
    }    

}