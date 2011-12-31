/*
 * Copyright (c) 2006-2011 DMDirc Developers
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
import com.dmdirc.ui.Colour;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class ColourManagerTest {

    @BeforeClass
    public static void setUp() throws Exception {
        IdentityManager.getIdentityManager().initialise();
    }

    @Test
    public void testGetColourInt() {
        int spec = 4;

        Colour expResult = Colour.RED;
        Colour result = ColourManager.getColour(spec);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetColourOOB() {
        Colour result = ColourManager.getColour(20);

        assertEquals(Colour.WHITE, result);
    }

    @Test
    public void testGetColourHexInvalid() {
        String spec = "FFZZFF";

        Colour result = ColourManager.getColour(spec);
        assertEquals(Colour.WHITE, result);
    }

    @Test
    public void testGetColourHex() {
        String spec = "FFFFFF";

        Colour expResult = Colour.WHITE;
        Colour result = ColourManager.getColour(spec);
        assertEquals(expResult, result);
    }

    @Test
    public void testParseColourNull() {
        Colour fallback = Colour.RED;
        Colour result = ColourManager.parseColour(null, fallback);

        assertEquals(fallback, result);
    }

    @Test
    public void testParseColourInvalidNumber() {
        Colour fallback = Colour.RED;
        Colour result = ColourManager.parseColour("zz", fallback);

        assertEquals(fallback, result);
    }

    @Test
    public void testParseColourOOBNumber() {
        Colour fallback = Colour.RED;
        Colour result = ColourManager.parseColour("20", fallback);

        assertEquals(fallback, result);
    }

    @Test
    public void testParseColourShortNumber() {
        Colour fallback = Colour.RED;
        Colour result = ColourManager.parseColour("1234", fallback);

        assertEquals(fallback, result);
    }

    @Test
    public void testColourCache() {
        Colour result1 = ColourManager.parseColour("ff0f0f");
        Colour result2 = ColourManager.parseColour("ff0f0f");
        Colour result3 = ColourManager.getColour("ff0f0f");

        assertSame(result1, result2);
        assertSame(result2, result3);
    }

    @Test
    public void testColourToHex() {
        Colour c1 = ColourManager.parseColour("ab3400");

        assertEquals("ab3400", ColourManager.getHex(c1).toLowerCase());
    }

    @Test
    public void testCustomColours() {
        IdentityManager.getConfigIdentity().setOption("colour", "4", "00ff00");

        assertEquals("00ff00", ColourManager.getHex(ColourManager.getColour(4)).toLowerCase());

        IdentityManager.getConfigIdentity().unsetOption("colour", "4");
    }

    @Test
    public void testCustomColours2() {
        IdentityManager.getConfigIdentity().setOption("colour", "4", "000000");
        IdentityManager.getConfigIdentity().unsetOption("colour", "4");
        assertEquals("ff0000", ColourManager.getHex(ColourManager.getColour(4)).toLowerCase());
    }
}
