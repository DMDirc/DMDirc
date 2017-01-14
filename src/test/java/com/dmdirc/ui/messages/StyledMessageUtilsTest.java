/*
 * Copyright (c) 2006-2017 DMDirc Developers
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


import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StyledMessageUtilsTest {

    private StyledMessageUtils styleUtils;

    @Before
    public void setUp() {
        styleUtils = new StyledMessageUtils();
    }

    @Test
    public void testStripControlCodes1() {
        final String input = "This"+ (char) 2 +" is "+ (char) 17 +"a test";

        final String expResult = "This is a test";
        final String result = styleUtils.stripControlCodes(input);
        assertEquals(expResult, result);
    }

    @Test
    public void testStripControlCodes2() {
        final String input = "This is "+ (char) 3 +"5a "+ (char) 4 +"FF0000test";

        final String expResult = "This is a test";
        final String result = styleUtils.stripControlCodes(input);
        assertEquals(expResult, result);
    }

    @Test
    public void testStripControlCodesNicknames() {
        final String input = "Link to \020Someone's long name\020\002Someone\002\020...";
        final String expResult = "Link to Someone...";
        final String result = styleUtils.stripControlCodes(input);
        assertEquals(expResult, result);
    }

}
