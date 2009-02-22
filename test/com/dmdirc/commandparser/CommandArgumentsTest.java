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

package com.dmdirc.commandparser;

import com.dmdirc.config.IdentityManager;

import java.util.Arrays;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class CommandArgumentsTest {

    @BeforeClass
    public static void beforeClass() {
        IdentityManager.load();
    }

    @Test
    public void testIsCommand() {
        assertTrue(new CommandArguments(CommandManager.getCommandChar() + "").isCommand());
        assertTrue(new CommandArguments(CommandManager.getCommandChar() + "foo bar").isCommand());
        assertFalse(new CommandArguments(" " + CommandManager.getCommandChar()).isCommand());
        assertFalse(new CommandArguments("").isCommand());
        assertFalse(new CommandArguments("foo").isCommand());
    }

    @Test
    public void testIsSilent() {
        final char c = CommandManager.getCommandChar();
        final char s = CommandManager.getSilenceChar();

        assertTrue(new CommandArguments(c + "" + s).isSilent());
        assertFalse(new CommandArguments("f" + s).isSilent());
        assertTrue(new CommandArguments(c + "" + s + "foo").isSilent());
        assertFalse(new CommandArguments("").isSilent());
        assertFalse(new CommandArguments("foo").isSilent());
    }

    @Test
    public void testGetLine() {
        assertEquals("foo", new CommandArguments("foo").getLine());
        assertEquals("foo  bar", new CommandArguments("foo  bar").getLine());
        assertEquals("", new CommandArguments("").getLine());
    }

    @Test
    public void testGetWords() {
        final CommandArguments args = new CommandArguments("a\tb    c d e");

        assertEquals(5, args.getWords().length);
        assertEquals("a", args.getWords()[0]);
        assertEquals("b", args.getWords()[1]);
        assertEquals("c", args.getWords()[2]);
        assertEquals("d", args.getWords()[3]);
        assertEquals("e", args.getWords()[4]);
    }

    @Test
    public void testGetArguments() {
        final CommandArguments args = new CommandArguments("a\tb    c d e");

        assertEquals(4, args.getArguments().length);
        assertEquals("b", args.getArguments()[0]);
        assertEquals("c", args.getArguments()[1]);
        assertEquals("d", args.getArguments()[2]);
        assertEquals("e", args.getArguments()[3]);
    }

    @Test
    public void testGetArgumentsAsString() {
        assertEquals("b\tc  d", new CommandArguments("a b\tc  d").getArgumentsAsString());
        assertEquals("", new CommandArguments("a").getArgumentsAsString());
        assertEquals("", new CommandArguments("a\t  \t   \t").getArgumentsAsString());
        assertEquals("b", new CommandArguments("a\t  \t   \tb").getArgumentsAsString());
    }

}