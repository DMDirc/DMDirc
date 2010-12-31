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

package com.dmdirc;

import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.harness.TestWritableFrameContainer;
import com.dmdirc.ui.interfaces.InputWindow;

import java.util.Arrays;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class WritableFrameContainerTest {

    @BeforeClass
    public static void setupClass() throws InvalidIdentityFileException {
        IdentityManager.load();
    }
    
    @Test
    public void testGetNumLines() {
        final WritableFrameContainer<?> container10
                = new TestWritableFrameContainer<InputWindow>(10, InputWindow.class);
        
        final int res0a = container10.getNumLines("");
        final int res0b = container10.getNumLines("\r");
        final int res0c = container10.getNumLines("\r\n");

        final int res1a = container10.getNumLines("0123456789");
        final int res1b = container10.getNumLines("\r\n123456789");
        final int res1c = container10.getNumLines("qaaa");

        final int res2a = container10.getNumLines("01234567890");
        final int res2b = container10.getNumLines("012345\r\n\r\n34567890");
        final int res2c = container10.getNumLines("01234567890\r\n\r\n");
        
        assertEquals(0, res0a);
        assertEquals(0, res0b);
        assertEquals(0, res0c);

        assertEquals(1, res1a);
        assertEquals(1, res1b);
        assertEquals(1, res1c);
        
        assertEquals(2, res2a);
        assertEquals(2, res2b);
        assertEquals(2, res2c);        
    }

    @Test
    public void testSplitLine() {
        final WritableFrameContainer<?> container10
                = new TestWritableFrameContainer<InputWindow>(10, InputWindow.class);
        final String[][][] tests = new String[][][]{
            {{""}, {""}},
            {{"0123456789"}, {"0123456789"}},
            {{"01234567890"}, {"0123456789", "0"}},
            {{"012345678→"}, {"012345678","→"}},
            {{"0123456→"}, {"0123456→"}},
            {{"01→2345678"}, {"01→23456","78"}},
            {{"01→23456\n78"}, {"01→23456","78"}},
            {{"01\n→2345678"}, {"01","→2345678"}},
            {{"→→→00"}, {"→→→0", "0"}},
        };

        for (String[][] test : tests) {
            final String[] res = container10.splitLine(test[0][0]).toArray(new String[0]);
            assertTrue("'" + test[0][0] + "' → "
                    + Arrays.toString(res) + " (expected: " + Arrays.toString(test[1]) + ")",
                    Arrays.equals(res, test[1]));
        }
    }

}
