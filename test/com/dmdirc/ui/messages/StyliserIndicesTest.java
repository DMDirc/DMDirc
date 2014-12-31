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

package com.dmdirc.ui.messages;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class StyliserIndicesTest {

    protected String input, output;
    protected int start, end;

    public StyliserIndicesTest(final String input, final int start, final int end, final String output) {
        this.input = input;
        this.start = start;
        this.end = end;
        this.output = output;
    }

    @Test
    public void testStyle() {
        assertEquals(output, Styliser.getStyledText(input, start, end));
    }

    @Parameterized.Parameters
    public static List<Object[]> data() {
        final Object[][] tests = {
            // No style
            {"Blah blah blah", 0, 1, "B"},
            {"Blah blah blah", 0, 4, "Blah"},
            {Styliser.CODE_BOLD + "Blah blah blah", 0, 4, Styliser.CODE_BOLD + "Blah"},
            {Styliser.CODE_BOLD + "Bl" + Styliser.CODE_BOLD + "ah blah blah",
                     0, 4, Styliser.CODE_BOLD + "Bl" + Styliser.CODE_BOLD + "ah"},
            {"Blah" + Styliser.CODE_BOLD + " blah blah",
                     0, 4, "Blah"},
            {"Blah" + Styliser.CODE_COLOUR + "4,0RED blah blah",
                     4, 7, Styliser.CODE_COLOUR + "4,0RED"},
            {"Blah" + Styliser.CODE_COLOUR + "4,0RED blah blah",
                     5, 7, "ED"},
            {Styliser.CODE_TOOLTIP + "Annoying internal tooltip text"
                     + Styliser.CODE_TOOLTIP + "Blah" + Styliser.CODE_TOOLTIP + "Blah",
                     2, 6, "ahBl"},
        };

        return Arrays.asList(tests);
    }

}
