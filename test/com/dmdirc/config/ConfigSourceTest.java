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
package com.dmdirc.config;

import com.dmdirc.harness.TestConfigSource;
import java.awt.Color;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConfigSourceTest extends junit.framework.TestCase {
    
    private final TestConfigSource s = new TestConfigSource();
    
    @Test
    public void testGetOption() {
        assertEquals("moo", s.getOption("false", "bar", "moo"));
        assertEquals("bar", s.getOption("true", "bar", "moo"));
    }
    
    @Test
    public void testGetColour() {
        assertEquals(Color.RED, s.getOptionColour("false", "moo", Color.RED));
        assertEquals(Color.WHITE, s.getOptionColour("true", "0", Color.RED));
    }
    
    @Test
    public void testGetChar() {
        assertEquals('c', s.getOptionChar("true", "c", 'd'));
        assertEquals('c', s.getOptionChar("true", "coo", 'd'));
        assertEquals('d', s.getOptionChar("false", "c", 'd'));
        assertEquals('d', s.getOptionChar("true", "", 'd'));
    }
    
    @Test
    public void testGetOptionalColour() {
        assertEquals(Color.RED, s.getOptionColour("true", "false:0", Color.RED));
        assertEquals(Color.WHITE, s.getOptionColour("true", "true:0", Color.RED));
    }    
    
    @Test
    public void testGetBoolean() {
        assertTrue(s.getOptionBool("true", "true"));
        assertFalse(s.getOptionBool("true", "false"));
        assertTrue(s.getOptionBool("true", "true", false));
        assertTrue(s.getOptionBool("false", "true", true));
        assertFalse(s.getOptionBool("false", "true", false));
    }
    
    @Test
    public void testGetInt() {
        assertEquals(42, s.getOptionInt("false", "moo", 42));
        assertEquals(42, s.getOptionInt("true", "42", 0));
        assertEquals(42, s.getOptionInt("true", "moo", 42));
    }
    
    @Test
    public void testGetList() {
        assertTrue(s.getOptionList("false", "moo").isEmpty());
        assertTrue(s.getOptionList("true", "").isEmpty());
        assertTrue(s.getOptionList("true", "\n\n\n").isEmpty());
        assertEquals(4, s.getOptionList("true", "\n\n\na", false).size());
        assertEquals(4, s.getOptionList("true", "a\nb\nc\nd", true).size());
        assertEquals("c", s.getOptionList("true", "a\nb\nc\nd", true).get(2));
    }

}