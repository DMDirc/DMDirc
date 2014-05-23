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

package com.dmdirc.commandparser.aliases;

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.interfaces.CommandController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AliasCommandHandlerTest {

    @Mock private FrameContainer container;
    @Mock private CommandController commandController;
    @Mock private CommandParser commandParser;
    @Mock private CommandContext context;
    @Mock private CommandInfo commandInfo;

    @Before
    public void setup() {
        when(container.getCommandParser()).thenReturn(commandParser);
        when(commandController.getCommandChar()).thenReturn('#');
        when(commandController.getSilenceChar()).thenReturn('/');
        when(context.getSource()).thenReturn(container);
        when(context.getCommandInfo()).thenReturn(commandInfo);
    }

    @Test
    public void testBasicAlias() {
        final Alias alias = new Alias(CommandType.TYPE_CHAT, "test", 0, "test2");
        final AliasCommandHandler handler = new AliasCommandHandler(commandController, alias);
        final CommandArguments arguments = new CommandArguments(commandController, "#test");
        handler.execute(container, arguments, context);
        verify(commandParser).parseCommand(container, "#test2");
    }

    @Test
    public void testSubstitutions() {
        final Alias alias = new Alias(CommandType.TYPE_CHAT, "test", 0, "test2 $1- $2 $1 $2- $2");
        final AliasCommandHandler handler = new AliasCommandHandler(commandController, alias);
        final CommandArguments arguments
                = new CommandArguments(commandController, "#test agadoo do");
        handler.execute(container, arguments, context);
        verify(commandParser).parseCommand(container, "#test2 agadoo do do agadoo do do");
    }

    @Test
    public void testInsufficientArgsSingular() {
        final Alias alias = new Alias(CommandType.TYPE_CHAT, "test", 1, "blah");
        final AliasCommandHandler handler = new AliasCommandHandler(commandController, alias);
        final CommandArguments arguments = new CommandArguments(commandController, "#test");
        handler.execute(container, arguments, context);
        verify(container).addLine("commandError", "test requires at least 1 argument.");
    }

    @Test
    public void testInsufficientArgsPlural() {
        final Alias alias = new Alias(CommandType.TYPE_CHAT, "test", 2, "blah");
        final AliasCommandHandler handler = new AliasCommandHandler(commandController, alias);
        final CommandArguments arguments = new CommandArguments(commandController, "#test agadoo");
        handler.execute(container, arguments, context);
        verify(container).addLine("commandError", "test requires at least 2 arguments.");
    }

    @Test
    public void testCarriesForwardSilence() {
        final Alias alias = new Alias(CommandType.TYPE_CHAT, "test", 0, "blah");
        final AliasCommandHandler handler = new AliasCommandHandler(commandController, alias);
        final CommandArguments arguments = new CommandArguments(commandController, "#/test agadoo");
        handler.execute(container, arguments, context);
        verify(commandParser).parseCommand(container, "#/blah");
    }

}
