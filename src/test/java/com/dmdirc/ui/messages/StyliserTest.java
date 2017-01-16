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

import com.dmdirc.config.provider.AggregateConfigProvider;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class StyliserTest {

    @Test
    public void testReadUntilControl1() {
        final String input = "This"+ (char) 2 +" is "+ (char) 17 +"a test";
        final String expResult = "This";
        final String result = Styliser.readUntilControl(input);
        assertEquals(expResult, result);
    }

    @Test
    public void testReadUntilControl2() {
        final String input = "This"+ (char) 17 +" is "+ (char) 17 +"a test";
        final String expResult = "This";
        final String result = Styliser.readUntilControl(input);
        assertEquals(expResult, result);
    }

    @Test
    public void testReadUntilControl3() {
        final String input = (char) 31 +" is "+ (char) 17 +"a test";
        final String expResult = "";
        final String result = Styliser.readUntilControl(input);
        assertEquals(expResult, result);
    }

    @Test
    @Ignore("Doesn't work in a headless environment (initialises an IRCDocument)")
    public void testNegation() {
        final String input1 = (char) 18 + "abc" + (char) 2 + "def" + (char) 31 + "ghi";
        final String input2 = "abcdefghi";

        final AggregateConfigProvider manager = mock(AggregateConfigProvider.class);
        final Styliser styliser = new Styliser(null, manager, new ColourManagerImpl(manager));

        for (int i = 0; i < input2.length(); i++) {
            // TODO...
            /*final Enumeration<?> res1 = styliser.getStyledString(new String[]{input1},
                    new StyledDocumentMaker()).getLogicalStyle(i).getAttributeNames();
            final Enumeration<?> res2 = styliser.getStyledString(new String[]{input2},
                    new StyledDocumentMaker()).getLogicalStyle(i).getAttributeNames();

            while (res1.hasMoreElements()) {
                assertEquals(res1.nextElement(), res2.nextElement());
            }

            assertFalse(res1.hasMoreElements());*/
        }
    }

}
