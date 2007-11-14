/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import org.junit.Test;
import static org.junit.Assert.*;


public class IgnoreListTest extends junit.framework.TestCase {
    
    private final String[][] tests = {
        {"a@b.c", "a@b\\.c"},
        {"*chris*", ".*chris.*"},
        {"c???s", "c...s"},
        {"c*?*", "c.*..*"},
    };  

    @Test
    public void testToRegex() {
        for (String[] test : tests) {
            final String convert1 = IgnoreList.simpleToRegex(test[0]);
            assertEquals(test[1], convert1);
        }
    }
    
    @Test
    public void testToSimple() {
        for (String[] test : tests) {
            final String convert2 = IgnoreList.regexToSimple(test[1]);
            assertEquals(test[0], convert2);
        }
    }    
    
}
