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

package com.dmdirc.commandparser.aliases;

import com.dmdirc.interfaces.CommandController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AliasFactoryTest {

    private static final String COMMAND_CHAR = "!";
    private static final String SILENCE_CHAR = "_";

    @Mock
    private CommandController commandController;
    private AliasFactory factory;

    @Before
    public void setup() {
        factory = new AliasFactory(commandController);

        when(commandController.getCommandChar()).thenReturn(COMMAND_CHAR.charAt(0));
        when(commandController.getSilenceChar()).thenReturn(SILENCE_CHAR.charAt(0));
    }

    @Test
    public void testSimpleAlias() {
        final Alias alias = factory.createAlias("name", 2, "response");
        assertEquals("name", alias.getName());
        assertEquals(2, alias.getMinArguments());
        assertEquals("response", alias.getSubstitution());
    }

    @Test
    public void testCommandChars() {
        final Alias alias = factory.createAlias(COMMAND_CHAR + "name", 2,
                COMMAND_CHAR + "response");
        assertEquals("name", alias.getName());
        assertEquals(2, alias.getMinArguments());
        assertEquals("response", alias.getSubstitution());
    }

    @Test
    public void testSilenceChars() {
        final Alias alias = factory.createAlias(SILENCE_CHAR + "name", 2,
                SILENCE_CHAR + "response");
        assertEquals(SILENCE_CHAR + "name", alias.getName());
        assertEquals(2, alias.getMinArguments());
        assertEquals(SILENCE_CHAR + "response", alias.getSubstitution());
    }

    @Test
    public void testCommandAndSilenceChars() {
        final Alias alias = factory.createAlias(COMMAND_CHAR + SILENCE_CHAR + "name", 2,
                COMMAND_CHAR + SILENCE_CHAR + "response");
        assertEquals("name", alias.getName());
        assertEquals(2, alias.getMinArguments());
        assertEquals("response", alias.getSubstitution());
    }

    @Test
    public void testMultilineResponse() {
        final Alias alias = factory.createAlias("name", 2,
                "response1\n"
                + "response2\r\n"
                + COMMAND_CHAR + "response3\r"
                + COMMAND_CHAR + SILENCE_CHAR + "response4\r\n\r\n"
                + SILENCE_CHAR + "response5\n\n\n");
        assertEquals("name", alias.getName());
        assertEquals(2, alias.getMinArguments());
        assertEquals("response1\r\nresponse2\r\nresponse3\r\nresponse4\r\n"
                + SILENCE_CHAR + "response5", alias.getSubstitution());
    }

}
