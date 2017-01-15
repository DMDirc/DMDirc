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
package com.dmdirc.commandparser.commands.global;

import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.aliases.AliasFactory;
import com.dmdirc.commandparser.aliases.AliasManager;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.events.CommandErrorEvent;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.ui.input.TabCompleterUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AliasCommandTest {

    @Mock private AliasFactory aliasFactory;
    @Mock private AliasManager aliasManager;
    @Mock private CommandController controller;
    @Mock private TabCompleterUtils tabCompleterUtils;
    @Mock private WindowModel tiw;
    @Mock private EventBus eventbus;
    private AliasCommand command;

    @Before
    public void setup() {
        command = new AliasCommand(controller, aliasFactory, aliasManager, tabCompleterUtils);
        when(controller.getCommandChar()).thenReturn('/');
        when(controller.getSilenceChar()).thenReturn('.');
        when(tiw.getEventBus()).thenReturn(eventbus);
    }

    @Test
    public void testUsageNoArgs() {
        command.execute(tiw, new CommandArguments(controller, "/foo"),
                new CommandContext(null, AliasCommand.INFO));

        verify(eventbus).publishAsync(isA(CommandErrorEvent.class));
    }

    @Test
    public void testUsageOneArg() {
        command.execute(tiw, new CommandArguments(controller, "/foo --remove"),
                new CommandContext(null, AliasCommand.INFO));

        verify(eventbus).publishAsync(isA(CommandErrorEvent.class));
    }

}