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

package com.dmdirc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;


public class IgnoreListTest {
    
    private final String[][] tests = {
        {"a@b.c", "a@b\\.c"},
        {"*chris*", ".*chris.*"},
        {"c???s", "c...s"},
        {"c*?*", "c.*..*"},
        {"foo?", "foo."},
        {".^$[]\\(){}|+", "\\.\\^\\$\\[\\]\\\\\\(\\)\\{\\}\\|\\+"},
    }; 
    
    private final String[] illegals = {
        "a+",
        "a*",
        "a.{4}",
        "a|b",
        "a?",
        "foo\\",
        "a\\?",
        "a\\*",
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
    
    @Test
    public void testIllegals() {
        for (String test : illegals) {
            boolean except = false;
            
            try {
                String converted = IgnoreList.regexToSimple(test);
            } catch (UnsupportedOperationException ex) {
                except = true;
            }
            
            assertTrue(except);
        }
    }
    
    @Test
    public void testConstructor() {
        final List<String> items = Arrays.asList(new String[]{"abc", "def"});
        final IgnoreList list = new IgnoreList(items);
        
        assertEquals(items, list.getRegexList());
    }
    
    @Test
    public void testAddSimple() {
        final IgnoreList list = new IgnoreList();
        
        for (String[] test : tests) {
            list.addSimple(test[0]);
            assertTrue(list.getRegexList().contains(test[1]));
        }
    }
    
    @Test
    public void testCanConvert() {
        final IgnoreList list = new IgnoreList();
        assertTrue(list.canConvert());
        
        list.addSimple("abc!def@ghi");
        assertTrue(list.canConvert());
        
        list.add(illegals[0]);
        assertFalse(list.canConvert());
    }
    
    @Test
    public void testGetSimpleList() throws UnsupportedOperationException {
        final IgnoreList list = new IgnoreList();
        final List<String> items = new ArrayList<String>();
        
        for (String[] test : tests) {
            items.add(test[0]);
            list.add(test[1]);
        }
        
        assertEquals(items, list.getSimpleList());
    }
    
}
