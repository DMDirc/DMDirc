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
package com.dmdirc.parser.irc;

import org.junit.Test;
import static org.junit.Assert.*;

public class IRCStringConverterTest {

    @Test
    public void testCaseConversion() {
        final IRCParser asciiParser = new IRCParser();
        asciiParser.updateCharArrays((byte) 0);

        final IRCParser rfcParser = new IRCParser();
        rfcParser.updateCharArrays((byte) 4);

        final IRCParser strictParser = new IRCParser();
        strictParser.updateCharArrays((byte) 3);

        final String[][] testcases = {
            {"12345", "12345", "12345", "12345"},
            {"HELLO", "hello", "hello", "hello"},
            {"^[[MOO]]^", "^[[moo]]^", "~{{moo}}~", "^{{moo}}^"},
            {"«—»", "«—»", "«—»", "«—»"},
        };

        for (String[] testcase : testcases) {
            final String asciiL = asciiParser.getStringConverter().toLowerCase(testcase[0]);
            final String rfcL = rfcParser.getStringConverter().toLowerCase(testcase[0]);
            final String strictL = strictParser.getStringConverter().toLowerCase(testcase[0]);

            final String asciiU = asciiParser.getStringConverter().toUpperCase(testcase[1]);
            final String rfcU = rfcParser.getStringConverter().toUpperCase(testcase[2]);
            final String strictU = strictParser.getStringConverter().toUpperCase(testcase[3]);

            assertEquals(testcase[1], asciiL);
            assertEquals(testcase[2], rfcL);
            assertEquals(testcase[3], strictL);

            assertTrue(asciiParser.getStringConverter().equalsIgnoreCase(testcase[0], testcase[1]));
            assertTrue(rfcParser.getStringConverter().equalsIgnoreCase(testcase[0], testcase[2]));
            assertTrue(strictParser.getStringConverter().equalsIgnoreCase(testcase[0], testcase[3]));

            assertEquals(testcase[0], asciiU);
            assertEquals(testcase[0], rfcU);
            assertEquals(testcase[0], strictU);
        }
    }
    
    @Test
    public void testLimit() {
        final IRCStringConverter ircsc = new IRCStringConverter((byte) 100);
        
        assertEquals(4, ircsc.getLimit());
    }
    
    @Test
    public void testEqualsNull() {
        final IRCStringConverter ircsc = new IRCStringConverter((byte) 100);
        
        assertTrue(ircsc.equalsIgnoreCase(null, null));
        assertFalse(ircsc.equalsIgnoreCase("null", null));
        assertFalse(ircsc.equalsIgnoreCase(null, "null"));
    }    

}