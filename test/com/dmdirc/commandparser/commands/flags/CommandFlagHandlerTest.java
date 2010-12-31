/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
package com.dmdirc.commandparser.commands.flags;

import com.dmdirc.config.InvalidIdentityFileException;
import java.util.Map;
import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.config.IdentityManager;
import java.util.Arrays;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class CommandFlagHandlerTest {
    
    private static final CommandFlag noArgsFlag1 = new CommandFlag("noargs1");
    private static final CommandFlag noArgsFlag2 = new CommandFlag("noargs2", false);
    private static final CommandFlag noArgsFlag3 = new CommandFlag("noargs3");
    private static final CommandFlag noArgsFlag4 = new CommandFlag("noargs4", false);
    private static final CommandFlag immArgsFlag1 = new CommandFlag("immargs1", true, 1, 0);
    private static final CommandFlag immArgsFlag2 = new CommandFlag("immargs2", true, 2, 0);
    private static final CommandFlag defArgsFlag1 = new CommandFlag("defargs1", true, 0, 1);
    private static final CommandFlag defArgsFlag2 = new CommandFlag("defargs2", true, 0, 2);
    private final CommandFlagHandler handler;
    
    private final String input;
    private final CommandFlag[] flags;
    private final int[] offsets;
    
    @BeforeClass
    public static void setupClass() throws InvalidIdentityFileException {
        IdentityManager.load();
    }
    
    public CommandFlagHandlerTest(String input, CommandFlag[] flags, int[] offsets) {
        noArgsFlag3.addEnabled(noArgsFlag4);
        noArgsFlag3.addDisabled(noArgsFlag1);

        handler = new CommandFlagHandler(noArgsFlag1, noArgsFlag2, noArgsFlag3,
                noArgsFlag4, immArgsFlag1, immArgsFlag2, defArgsFlag1, defArgsFlag2);
        
        this.input = input;
        this.flags = flags;
        this.offsets = offsets;
    }
    
    @Test
    public void testParse() {
        final FrameContainer<?> container = mock(FrameContainer.class);
        
        final Map<CommandFlag, Integer> results
                = handler.parse(container, new CommandArguments(input));
        
        if (flags == null) {
            assertNull("Command should fail: " + input, results);
        } else {
            assertNotNull("Command should NOT fail: " + input, results);
            assertTrue("Result must contain a null element: " + input,
                    results.containsKey(null));

            int i = 0;
            for (CommandFlag flag : flags) {
                assertTrue("Result must contain flag: " + flag.getName()
                        + ": " + input, results.containsKey(flag));
                assertEquals("Offset for flag " + flag.getName()
                        + " should be " + offsets[i] + ": " + input,
                        offsets[i++], (int) results.get(flag));
            }

            assertEquals("Global offset should be " + offsets[i],
                    offsets[i], (int) results.get(null));

            assertEquals("Result should contain " + (flags.length + 1)
                    + " elements: " + input, flags.length + 1, results.size());
        }
    }

    @Parameterized.Parameters
    public static List<Object[]> data() {
        final Object[][] tests = {
            {"/foo", new CommandFlag[0], new int[]{0}},
            {"/foo --noargs1", new CommandFlag[]{noArgsFlag1}, new int[]{1, 1}},
            {"/foo --noargs2", null, null},
            {"/foo --noargs3", new CommandFlag[]{noArgsFlag3}, new int[]{1, 1}},
            {"/foo --noargs4", null, null},
            {"/foo --noargs5", new CommandFlag[0], new int[]{0}},
            {"/foo --noargs3 --noargs4", new CommandFlag[]{noArgsFlag3, noArgsFlag4}, new int[]{1, 2, 2}},
            {"/foo --noargs3 --noargs1", null, null},
            {"/foo --noargs1 --noargs3", new CommandFlag[]{noArgsFlag1, noArgsFlag3}, new int[]{1, 2, 2}},
            {"/foo --noargs3 --noargs4 --noargs1", null, null},
            {"/foo --noargs3 --noargs4 --noargs2", null, null},

            {"/foo --immargs1", null, null},
            {"/foo --noargs1 --immargs1", null, null},
            {"/foo --immargs2", null, null},
            {"/foo --immargs2 xxx", null, null},
            {"/foo --immargs1 arg1", new CommandFlag[]{immArgsFlag1}, new int[]{1, 2}},
            {"/foo --noargs1 --immargs1 arg1", new CommandFlag[]{immArgsFlag1, noArgsFlag1},
                    new int[]{2, 1, 3}},
            {"/foo --immargs1 arg1 --noargs1", new CommandFlag[]{immArgsFlag1, noArgsFlag1},
                    new int[]{1, 3, 3}},

            {"/foo --defargs1", null, null},
            {"/foo --noargs1 --defargs1", null, null},
            {"/foo --defargs2", null, null},
            {"/foo --defargs2 xxx", null, null},
            {"/foo --defargs1 arg1", new CommandFlag[]{defArgsFlag1}, new int[]{1, 2}},
            {"/foo --noargs1 --defargs1 arg1", new CommandFlag[]{defArgsFlag1, noArgsFlag1},
                    new int[]{2, 1, 3}},
            {"/foo --defargs1 --noargs1 arg1", new CommandFlag[]{defArgsFlag1, noArgsFlag1},
                    new int[]{2, 2, 3}},
        };

        return Arrays.asList(tests);
    }    

}
