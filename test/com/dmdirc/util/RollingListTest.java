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

public class RollingListTest {

    @Test
    public void testIsEmpty() {
        final RollingList<String> rl = new RollingList<String>(1);
        assertTrue(rl.isEmpty());
        assertTrue(rl.getList().isEmpty());
        
        rl.add("Foo");
        assertFalse(rl.isEmpty());
        assertFalse(rl.getList().isEmpty());
    }
    
    @Test
    public void testRolling() {
        final RollingList<String> rl = new RollingList<String>(1);
        
        rl.add("Foo");
        rl.add("Bar");
        rl.add("Baz");

        assertEquals("Baz", rl.get(0));
        assertEquals(1, rl.getList().size());
        assertFalse(rl.contains("Bar"));
    }
    
    @Test
    public void testClear() {
        final RollingList<String> rl = new RollingList<String>(3);
        
        rl.add("Foo");
        rl.add("Bar");
        rl.add("Baz");
        rl.clear();
        
        assertTrue(rl.isEmpty());
        assertTrue(rl.getList().isEmpty());
    }
    
    @Test
    public void testPositions() {
        final RollingList<String> rl = new RollingList<String>(3);
        
        rl.add("Foo");
        rl.add("Bar");
        rl.add("Baz");
        
        assertEquals(0, rl.getPosition());
        
        rl.seekToEnd();
        assertEquals(3, rl.getPosition());
        
        rl.seekToStart();
        assertEquals(0, rl.getPosition());
    }
    
    @Test
    public void testPrevNext() {
        final RollingList<String> rl = new RollingList<String>(3);
        
        rl.add("Foo");
        rl.add("Bar");
        rl.add("Baz");
        
        assertEquals("Bar", rl.getNext());
        assertEquals("Baz", rl.getNext());
        assertFalse(rl.hasNext());
        assertTrue(rl.hasPrevious());
        
        assertEquals("Bar", rl.getPrevious());
        assertEquals("Foo", rl.getPrevious());
        assertFalse(rl.hasPrevious());
        assertTrue(rl.hasNext());
    }
    
    @Test
    public void testEmpty() {
        final RollingList<String> rl = new RollingList<String>(1, "Meep");
        rl.add("Foo");
        
        assertEquals("Meep", rl.getNext());
        assertFalse(rl.hasNext());
        
        rl.add("Bar");
        
        // The position moves when adding
        assertTrue(rl.hasNext());
        assertFalse(rl.hasPrevious());
        assertEquals("Meep", rl.getNext());
        assertEquals("Bar", rl.getPrevious());
    }

}
