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

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class MapListTest {

    @Test
    public void testIsEmpty() {
        final MapList<String, String> test = new MapList<String, String>();
        assertTrue(test.isEmpty());

        test.add("a", "b");
        assertFalse(test.isEmpty());
        test.removeFromAll("b");
        assertTrue(test.isEmpty());
    }
    
    @Test
    public void testAddCollection() {
        final MapList<String, String> test = new MapList<String, String>();
        final List<String> testList = new ArrayList<String>();
        testList.add("d");
        testList.add("e");
        test.add("key", testList);
        
        assertTrue(test.containsKey("key"));
        assertTrue(test.containsValue("key", "d"));
        assertTrue(test.containsValue("key", "e"));
    }
    
    @Test
    public void testClear() {
        final MapList<String, String> test = new MapList<String, String>();
        test.add("a", "b");
        test.add("d", "e");
        test.clear();
        assertTrue(test.isEmpty());
    }
    
    @Test
    public void testClearKey() {
        final MapList<String, String> test = new MapList<String, String>();
        test.add("a", "b");
        test.add("d", "e");
        test.clear("a");
        assertTrue(test.values("a").isEmpty());
        assertFalse(test.isEmpty());
    }
    
    @Test
    public void testRemove() {
        final MapList<String, String> test = new MapList<String, String>();
        test.add("a", "b");
        test.add("d", "e");
        test.remove("z", "b");
        
        assertEquals(2, test.keySet().size());
        assertEquals(1, test.values("a").size());
        assertEquals(1, test.values("d").size());
        
        test.remove("a", "b");
        assertEquals(2, test.keySet().size());
        assertEquals(0, test.values("a").size());
        assertEquals(1, test.values("d").size());        
    }    
    
    @Test
    public void testKeySet() {
        final MapList<String, String> test = new MapList<String, String>();
        test.add("a", "b");
        test.add("d", "e");
        assertEquals(2, test.keySet().size());
        assertTrue(test.keySet().contains("a"));
        assertTrue(test.keySet().contains("d"));
    }    

    @Test
    public void testContainsKey() {
        final MapList<String, String> test = new MapList<String, String>();
        test.add("a", "b");
        assertTrue(test.containsKey("a"));
    }

    @Test
    public void testContainsValue() {
        final MapList<String, String> test = new MapList<String, String>();
        test.add("a", "b");
        assertTrue(test.containsValue("a", "b"));
    }

    @Test
    public void testGet() {
        final MapList<String, String> test = new MapList<String, String>();
        test.add("a", "b");
        assertEquals(1, test.get("a").size());
        assertEquals("b", test.get("a").get(0));
        assertEquals("b", test.get("a", 0));
    }
    
    @Test
    public void testInherit() {
        final MapList<String, String> test1 = new MapList<String, String>();
        test1.add("a", "b");
        
        final MapList<String, String> test2 = new MapList<String, String>(test1);
        assertEquals(1, test2.get("a").size());
        assertEquals("b", test2.get("a").get(0));
        assertEquals("b", test2.get("a", 0));        
    }
    
    @Test
    public void testGetMap() {
        final MapList<String, String> test1 = new MapList<String, String>();
        test1.add("a", "b");
        assertNotSame(test1.getMap(), test1.getMap());
        
        final MapList<String, String> test2 = new MapList<String, String>(test1);
        assertEquals(test1.getMap(), test2.getMap());
    }    

}
