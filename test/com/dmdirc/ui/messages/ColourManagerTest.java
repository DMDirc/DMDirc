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

package com.dmdirc.ui.messages;

import com.dmdirc.config.IdentityManager;

import java.awt.Color;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ColourManagerTest extends junit.framework.TestCase {

    @Before
    public void setUp() throws Exception {
        IdentityManager.load();
    }

    @Test
    public void testGetColourInt() {
        int spec = 4;

        Color expResult = Color.RED;
        Color result = ColourManager.getColour(spec);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetColourOOB() {
        Color result = ColourManager.getColour(20);
        
        assertEquals(Color.WHITE, result);
    }
    
    @Test
    public void testGetColourHexInvalid() {
        String spec = "FFZZFF";
        
        Color result = ColourManager.getColour(spec);
        assertEquals(Color.WHITE, result);
    }

    @Test
    public void testGetColourHex() {
        String spec = "FFFFFF";

        Color expResult = Color.decode("#FFFFFF");
        Color result = ColourManager.getColour(spec);
        assertEquals(expResult, result);
    }

    @Test
    public void testParseColourNull() {
        Color fallback = Color.RED;
        Color result = ColourManager.parseColour(null, fallback);

        assertEquals(fallback, result);
    }

    @Test
    public void testParseColourInvalidNumber() {
        Color fallback = Color.RED;
        Color result = ColourManager.parseColour("zz", fallback);

        assertEquals(fallback, result);
    }

    @Test
    public void testParseColourOOBNumber() {
        Color fallback = Color.RED;
        Color result = ColourManager.parseColour("20", fallback);

        assertEquals(fallback, result);
    }

    @Test
    public void testParseColourShortNumber() {
        Color fallback = Color.RED;
        Color result = ColourManager.parseColour("1234", fallback);

        assertEquals(fallback, result);
    }

    @Test
    public void testColourCache() {
        Color result1 = ColourManager.parseColour("ff0f0f");
        Color result2 = ColourManager.parseColour("ff0f0f");
        Color result3 = ColourManager.getColour("ff0f0f");

        assertSame(result1, result2);
        assertSame(result2, result3);
    }
    
    @Test
    public void testColourToHex() {
        Color c1 = ColourManager.parseColour("ab3400");
        
        assertEquals("ab3400", ColourManager.getHex(c1).toLowerCase());
    }

}
