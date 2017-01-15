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
package com.dmdirc.commandparser.commands.flags;

import com.dmdirc.events.eventbus.MBassadorEventBus;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.WindowModel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class CommandFlagHandlerTest {

    private static final CommandFlag NO_ARGS_FLAG_1 = new CommandFlag("noargs1");
    private static final CommandFlag NO_ARGS_FLAG_2 = new CommandFlag("noargs2", false);
    private static final CommandFlag NO_ARGS_FLAG_3 = new CommandFlag("noargs3");
    private static final CommandFlag NO_ARGS_FLAG_4 = new CommandFlag("noargs4", false);
    private static final CommandFlag IMM_ARGS_FLAG_1 = new CommandFlag("immargs1", true, 1, 0);
    private static final CommandFlag IMM_ARGS_FLAG_2 = new CommandFlag("immargs2", true, 2, 0);
    private static final CommandFlag DEF_ARGS_FLAG_1 = new CommandFlag("defargs1", true, 0, 1);
    private static final CommandFlag DEF_ARGS_FLAG_2 = new CommandFlag("defargs2", true, 0, 2);
    private final CommandFlagHandler handler;

    private final String input;
    private final CommandFlag[] flags;
    private final int[] offsets;

    public CommandFlagHandlerTest(final String input, final CommandFlag[] flags, final int[] offsets) {
        NO_ARGS_FLAG_3.addEnabled(NO_ARGS_FLAG_4);
        NO_ARGS_FLAG_3.addDisabled(NO_ARGS_FLAG_1);

        handler = new CommandFlagHandler(NO_ARGS_FLAG_1, NO_ARGS_FLAG_2, NO_ARGS_FLAG_3,
                NO_ARGS_FLAG_4, IMM_ARGS_FLAG_1, IMM_ARGS_FLAG_2, DEF_ARGS_FLAG_1, DEF_ARGS_FLAG_2);

        this.input = input;
        this.flags = flags;
        this.offsets = offsets;
    }

    @Test
    public void testParse() {
        final WindowModel container = mock(WindowModel.class);
        final EventBus eventBus = mock(MBassadorEventBus.class);
        final CommandController controller = mock(CommandController.class);
        when(controller.getCommandChar()).thenReturn('/');
        when(controller.getSilenceChar()).thenReturn('.');
        when(container.getEventBus()).thenReturn(eventBus);

        final Map<CommandFlag, Integer> results = handler.parse(container,
                new CommandArguments(controller, input));

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
            {"/foo --noargs1", new CommandFlag[]{NO_ARGS_FLAG_1}, new int[]{1, 1}},
            {"/foo --noargs2", null, null},
            {"/foo --noargs3", new CommandFlag[]{NO_ARGS_FLAG_3}, new int[]{1, 1}},
            {"/foo --noargs4", null, null},
            {"/foo --noargs5", new CommandFlag[0], new int[]{0}},
            {"/foo --noargs3 --noargs4", new CommandFlag[]{NO_ARGS_FLAG_3, NO_ARGS_FLAG_4}, new int[]{1, 2, 2}},
            {"/foo --noargs3 --noargs1", null, null},
            {"/foo --noargs1 --noargs3", new CommandFlag[]{NO_ARGS_FLAG_1, NO_ARGS_FLAG_3}, new int[]{1, 2, 2}},
            {"/foo --noargs3 --noargs4 --noargs1", null, null},
            {"/foo --noargs3 --noargs4 --noargs2", null, null},

            {"/foo --immargs1", null, null},
            {"/foo --noargs1 --immargs1", null, null},
            {"/foo --immargs2", null, null},
            {"/foo --immargs2 xxx", null, null},
            {"/foo --immargs1 arg1", new CommandFlag[]{IMM_ARGS_FLAG_1}, new int[]{1, 2}},
            {"/foo --noargs1 --immargs1 arg1", new CommandFlag[]{IMM_ARGS_FLAG_1, NO_ARGS_FLAG_1},
                    new int[]{2, 1, 3}},
            {"/foo --immargs1 arg1 --noargs1", new CommandFlag[]{IMM_ARGS_FLAG_1, NO_ARGS_FLAG_1},
                    new int[]{1, 3, 3}},

            {"/foo --defargs1", null, null},
            {"/foo --noargs1 --defargs1", null, null},
            {"/foo --defargs2", null, null},
            {"/foo --defargs2 xxx", null, null},
            {"/foo --defargs1 arg1", new CommandFlag[]{DEF_ARGS_FLAG_1}, new int[]{1, 2}},
            {"/foo --noargs1 --defargs1 arg1", new CommandFlag[]{DEF_ARGS_FLAG_1, NO_ARGS_FLAG_1},
                    new int[]{2, 1, 3}},
            {"/foo --defargs1 --noargs1 arg1", new CommandFlag[]{DEF_ARGS_FLAG_1, NO_ARGS_FLAG_1},
                    new int[]{2, 2, 3}},
        };

        return Arrays.asList(tests);
    }

}
