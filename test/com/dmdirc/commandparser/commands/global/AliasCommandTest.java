/*
 * Copyright (c) 2006-2012 DMDirc Developers
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

import com.dmdirc.TestMain;
import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.CommandContext;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class AliasCommandTest {

    private final AliasCommand command = new AliasCommand();

    @BeforeClass
    public static void setUpClass() throws Exception {
        TestMain.getTestMain();
    }

    @Test
    public void testUsageNoArgs() {
        final FrameContainer tiw = mock(FrameContainer.class);

        command.execute(tiw, new CommandArguments("/foo"),
                new CommandContext(null, AliasCommand.INFO));

        verify(tiw).addLine(eq("commandUsage"), anyChar(), anyString(), anyString());
    }

    @Test
    public void testUsageOneArg() {
        final FrameContainer tiw = mock(FrameContainer.class);

        command.execute(tiw, new CommandArguments("/foo --remove"),
                new CommandContext(null, AliasCommand.INFO));

        verify(tiw).addLine(eq("commandUsage"), anyChar(), anyString(), anyString());
    }

}