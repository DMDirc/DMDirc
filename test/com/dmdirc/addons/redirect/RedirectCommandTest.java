/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.redirect;

import com.dmdirc.MessageTarget;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.commandparser.parsers.ServerCommandParser;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.ui.interfaces.InputWindow;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class RedirectCommandTest {

    @BeforeClass
    public static void setupClass() throws InvalidIdentityFileException {
        IdentityManager.load();
        CommandManager.initCommands();
    }

    @Test
    public void testExecute() {
        final RedirectCommand command = new RedirectCommand();
        final MessageTarget target = mock(MessageTarget.class);
        final InputWindow window = mock(InputWindow.class);
        when(target.getFrame()).thenReturn(window);
        final CommandParser parser = new ServerCommandParser(null);
        when(window.getCommandParser()).thenReturn(parser);
        when(window.getConfigManager()).thenReturn(IdentityManager.getGlobalConfig());

        command.execute(window, null, target, false, new CommandArguments("/redirect /echo test"));
        
        verify(target).sendLine("test");
    }

}