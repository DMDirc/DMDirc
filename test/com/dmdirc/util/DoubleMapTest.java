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

package com.dmdirc.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class DoubleMapTest {

    @Test
    public void testPut() {
        final DoubleMap<String, String> dm = new DoubleMap<String, String>();
        dm.put("a", "b");
        
        assertEquals(1, dm.keySet().size());
        assertEquals(1, dm.valueSet().size());
        assertTrue(dm.keySet().contains("a"));
        assertTrue(dm.valueSet().contains("b"));
    }
    
    @Test(expected=NullPointerException.class)
    public void testPutNull1() {
        final DoubleMap<String, String> dm = new DoubleMap<String, String>();
        dm.put(null, "b");
    }
    
    @Test(expected=NullPointerException.class)
    public void testPutNull2() {
        final DoubleMap<String, String> dm = new DoubleMap<String, String>();
        dm.put("a", null);
    }    
    
    @Test
    public void testGet() {
        final DoubleMap<String, String> dm = new DoubleMap<String, String>();
        dm.put("a", "b");
        dm.put("b", "c");
        dm.put("c", "a");
        
        assertEquals("b", dm.getValue("a"));
        assertEquals("b", dm.getKey("c"));
        assertEquals("c", dm.getKey("a"));
    }    

}