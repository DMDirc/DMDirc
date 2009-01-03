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
package com.dmdirc.commandparser.commands;

import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.CommandType;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class HelpTest {

    private Command command;

    public HelpTest(final Command command) {
        this.command = command;
    }

    @Test
    public void testHelp() {
        assertTrue("Command's help output should start with its name ("
                + command.getName() + ")", command.getHelp().startsWith(command.getName()));
    }

    @Parameterized.Parameters
    public static List<Object[]> data() {
        final List<Object[]> res = new LinkedList<Object[]>();

        CommandManager.initCommands();

        for (CommandType type : CommandType.values()) {
            for (Command command : CommandManager.getCommands(type)) {
                if (command.showInHelp()) {
                    res.add(new Object[]{command});
                }
            }
        }

        return res;
    }
    
    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(HelpTest.class);
    }
    
}