/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
package com.dmdirc.plugins;

import org.junit.Test;
import static org.junit.Assert.*;

public class PluginInfoTest extends junit.framework.TestCase {
    
    private PluginInfo pi;
    
    public PluginInfoTest() {
        try {
            pi = new PluginInfo("moo", false);
        } catch (PluginException ex) {
            // Shouldn't happen
        }
    }

    @Test
    public void testCheckMinimum() {
        assertTrue(pi.checkMinimumVersion("5", 6));
        assertTrue(pi.checkMinimumVersion("5", 5));
        assertTrue(pi.checkMinimumVersion("0", 17));
        assertTrue(pi.checkMinimumVersion("100", 0));
        assertTrue(pi.checkMinimumVersion("0", 0));
        assertFalse(pi.checkMinimumVersion("abc", 6));
        assertFalse(pi.checkMinimumVersion("7", 6));
    }
    
    @Test
    public void testCheckMaximim() {
        assertTrue(pi.checkMaximumVersion("6", 6));
        assertTrue(pi.checkMaximumVersion("7", 6));
        assertTrue(pi.checkMaximumVersion("0", 6));
        assertTrue(pi.checkMaximumVersion("6", 0));
        assertTrue(pi.checkMaximumVersion("0", 0));
        assertTrue(pi.checkMaximumVersion("", 17));
        assertFalse(pi.checkMaximumVersion("abc", 6));
        assertFalse(pi.checkMaximumVersion("7", 10));
    }

}