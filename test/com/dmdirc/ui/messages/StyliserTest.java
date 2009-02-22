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

package com.dmdirc.ui.messages;

import com.dmdirc.config.IdentityManager;
import java.util.Enumeration;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class StyliserTest {
    
    @Before
    public void setUp() {
        IdentityManager.load();
    }
    
    @Test
    public void testStripControlCodes1() {
        String input = "This"+((char) 2)+" is "+((char) 17)+"a test";
        
        String expResult = "This is a test";
        String result = Styliser.stipControlCodes(input);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testStripControlCodes2() {
        String input = "This is "+((char) 3)+"5a "+((char) 4)+"FF0000test";
        
        String expResult = "This is a test";
        String result = Styliser.stipControlCodes(input);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testReadUntilControl1() {
        String input = "This"+((char) 2)+" is "+((char) 17)+"a test";
        String expResult = "This";
        String result = Styliser.readUntilControl(input);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testReadUntilControl2() {
        String input = "This"+((char) 17)+" is "+((char) 17)+"a test";
        String expResult = "This";
        String result = Styliser.readUntilControl(input);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testReadUntilControl3() {
        String input = ((char) 31)+" is "+((char) 17)+"a test";
        String expResult = "";
        String result = Styliser.readUntilControl(input);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testNegation() {
        final String input1 = ((char) 18) + "abc" + ((char) 2) + "def" + ((char) 31) + "ghi";
        final String input2 = "abcdefghi";
        
        for (int i = 0; i < input2.length(); i++) {
            final Enumeration<?> res1 = Styliser.getStyledString(new String[]{input1})
                    .getLogicalStyle(i).getAttributeNames();
            final Enumeration<?> res2 = Styliser.getStyledString(new String[]{input2})
                    .getLogicalStyle(i).getAttributeNames();

            while (res1.hasMoreElements()) {
                assertEquals(res1.nextElement(), res2.nextElement());
            }

            assertFalse(res1.hasMoreElements());
        }
    }
    
}
