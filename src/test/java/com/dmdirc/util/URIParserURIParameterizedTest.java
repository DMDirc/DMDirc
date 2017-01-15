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
package com.dmdirc.util;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class URIParserURIParameterizedTest {

    private final String input, expected;

    public URIParserURIParameterizedTest(final String input, final String expected) {
        this.input = input;
        this.expected = expected;
    }

    @Test
    public void testURLParser() throws InvalidURIException {
        final URIParser uriParser = new URIParser();
        assertEquals(expected, uriParser.parseFromURI(input).toString());
    }

    @Parameters(name = "{index}: {1}")
    public static List<String[]> getData() {
        return Arrays.asList(new String[][]{
            {"irc://irc.test.com", "irc://irc.test.com"},
            {"irc://irc.test.com:6667", "irc://irc.test.com:6667"},
            {"irc://irc.test.com:+6667", "ircs://irc.test.com:6667"},
            {"ircs://irc.test.com:+6667", "ircs://irc.test.com:6667"},
            {"ircs://irc.test.com:+6667", "ircs://irc.test.com:6667"},
            {"ircs://username@irc.test.com:+6667", "ircs://username@irc.test.com:6667"},});
    }

}
