/*
 * Copyright (c) 2006-2017 DMDirc Developers
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

import com.dmdirc.interfaces.CommandController;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CommandArgumentsTest {

    @Mock private CommandController controller;

    @Before
    public void setUp() {
        when(this.controller.getCommandChar()).thenReturn('/');
        when(this.controller.getSilenceChar()).thenReturn('.');
    }

    /** Ensures the ctor which takes a collection builds the 'line' property. */
    @Test
    public void testCollectionCtorCreatesLine() {
        final CommandArguments args = new CommandArguments(controller,
                Arrays.asList("/command", "arg1", "arg2"));
        assertEquals("/command arg1 arg2", args.getLine());
    }

    /** Ensures the ctor works with an empty collection. */
    @Test
    public void testCollectionCtorWithEmpty() {
        final CommandArguments args = new CommandArguments(
                controller, Arrays.asList(new String[0]));
        assertEquals("", args.getLine());
    }

    /** Ensures that getStrippedLine returns non-command lines as-is. */
    @Test
    public void testGetStrippedLineNormal() {
        final CommandArguments args = new CommandArguments(controller, "blah blah");
        assertEquals("blah blah", args.getStrippedLine());
    }

    /**
     * Ensures that getStrippedLine returns command lines without the command
     * char.
     */
    @Test
    public void testGetStrippedLineCommand() {
        final CommandArguments args = new CommandArguments(controller, "/blah blah");
        assertEquals("blah blah", args.getStrippedLine());
    }

    /**
     * Ensures that getStrippedLine returns silenced lines without the
     * command or silence chars.
     */
    @Test
    public void testGetStrippedLineSilenced() {
        final CommandArguments args = new CommandArguments(controller, "/.blah blah");
        assertEquals("blah blah", args.getStrippedLine());
    }

    @Test
    public void testIsCommand() {
        assertTrue(new CommandArguments(controller, "/").isCommand());
        assertTrue(new CommandArguments(controller, "/foo bar").isCommand());
        assertFalse(new CommandArguments(controller, " /").isCommand());
        assertFalse(new CommandArguments(controller, "").isCommand());
        assertFalse(new CommandArguments(controller, "foo").isCommand());
    }

    @Test
    public void testIsSilent() {
        final char c = '/';
        final char s = '.';

        assertTrue(new CommandArguments(controller, Character.toString(c) + s).isSilent());
        assertFalse(new CommandArguments(controller, "f" + s).isSilent());
        assertTrue(new CommandArguments(controller, Character.toString(c) + s + "foo").isSilent());
        assertFalse(new CommandArguments(controller, "").isSilent());
        assertFalse(new CommandArguments(controller, "foo").isSilent());
    }

    @Test
    public void testGetLine() {
        assertEquals("foo", new CommandArguments(controller, "foo").getLine());
        assertEquals("foo  bar", new CommandArguments(controller, "foo  bar").getLine());
        assertEquals("", new CommandArguments(controller, "").getLine());
    }

    @Test
    public void testGetWords() {
        final CommandArguments args = new CommandArguments(controller, "a\tb    c d e");

        assertEquals(5, args.getWords().length);
        assertEquals("a", args.getWords()[0]);
        assertEquals("b", args.getWords()[1]);
        assertEquals("c", args.getWords()[2]);
        assertEquals("d", args.getWords()[3]);
        assertEquals("e", args.getWords()[4]);
    }

    @Test
    public void testGetArguments() {
        final CommandArguments args = new CommandArguments(controller, "a\tb    c d e");

        assertEquals(4, args.getArguments().length);
        assertEquals("b", args.getArguments()[0]);
        assertEquals("c", args.getArguments()[1]);
        assertEquals("d", args.getArguments()[2]);
        assertEquals("e", args.getArguments()[3]);
    }

    @Test
    public void testGetArgumentsAsString() {
        assertEquals("b\tc  d", new CommandArguments(controller, "a b\tc  d").getArgumentsAsString());
        assertEquals("", new CommandArguments(controller, "a").getArgumentsAsString());
        assertEquals("", new CommandArguments(controller, "a\t  \t   \t").getArgumentsAsString());
        assertEquals("b", new CommandArguments(controller, "a\t  \t   \tb").getArgumentsAsString());
    }

}