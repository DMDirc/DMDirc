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

package com.dmdirc.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class URLHandlerTest {
    
    @Test
    public void testParseArguments() {
        final String[][][] tests = new String[][][]{
            {{"abcdef abcdef abcdef"}, {"abcdef", "abcdef", "abcdef"}},
            {{"abcdef \"abcdef abcdef\""}, {"abcdef", "abcdef abcdef"}},
            {{"abcdef \"abcdef foo abcdef\""}, {"abcdef", "abcdef foo abcdef"}},
            {{"abcdef \"abcdef\" \"abcdef\""}, {"abcdef", "abcdef", "abcdef"}},
            {{"abcdef \"\""}, {"abcdef", ""}},
            {{"abcdef \" foo?\""}, {"abcdef", " foo?"}},
        };
        
        for (String[][] test : tests) {
            final String[] res = URLHandler.parseArguments(test[0][0]);
            assertTrue(test[0][0] + " - " + Arrays.toString(test[1]) + " - "
                    + Arrays.toString(res),
                    Arrays.equals(test[1], res));
        }
    }

    @Test
    public void testSubstituteParams() throws MalformedURLException, URISyntaxException {
        final Object[][] tests = new Object[][]{
            {new URI("protocol://host/path"), "$protocol $host $path", "protocol host /path"},
            {new URI("protocol://host"), "$protocol $host $path", "protocol host "},
            {new URI("protocol://host:33"), "$port", "33"},
            {new URI("http://host/foo"), "$port", ""},
            {new URI("http://blarg!@host/foo"), "$$$username$$$", "$$blarg!$$$"},
            {new URI("http://blarg!:flub@host/foo"), "password$password", "passwordflub"},
            {new URI("protocol://@host:33"), "$username", ""},
            {new URI("protocol://@host:33/?foo+bar#frag"), "$query $fragment", "foo+bar frag"},
            {new URI("host.com"), "$path $protocol$host", "host.com "},
        };
        
        for (Object[] test : tests) {
            final String result = URLHandler.getURLHander().substituteParams((URI) test[0],
                    (String) test[1]);
            assertEquals(test[0].toString() + " + " + test[1].toString() + " ==> " + result,
                    (String) test[2], result);
        }
    }
    
    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(URLHandlerTest.class);
    }
    
}