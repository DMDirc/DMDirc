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

import org.junit.Test;


public class URIParserTest {

    @Test(expected = InvalidURIException.class)
    public void testURINoScheme() throws InvalidURIException {
        final URIParser uriParser = new URIParser();
        uriParser.parseFromURI("irc.test.com");
    }

    @Test(expected = InvalidURIException.class)
    public void testURINoAuthority() throws InvalidURIException {
        final URIParser uriParser = new URIParser();
        uriParser.parseFromURI("irc://");
    }

    @Test(expected = InvalidURIException.class)
    public void testURIInvalidHostname() throws InvalidURIException {
        final URIParser uriParser = new URIParser();
        uriParser.parseFromURI("irc://irc_test.com");
    }

    @Test(expected = InvalidURIException.class)
    public void testURIInvalidUserInfo() throws InvalidURIException {
        final URIParser uriParser = new URIParser();
        uriParser.parseFromURI("ircs://:irc.test.com:6667");
    }

    @Test(expected = InvalidURIException.class)
    public void testURIEmptyPort() throws InvalidURIException {
        final URIParser uriParser = new URIParser();
        uriParser.parseFromURI("ircs://irc.test.com:");
    }

    @Test(expected = InvalidURIException.class)
    public void testURIInvalidPort() throws InvalidURIException {
        final URIParser uriParser = new URIParser();
        uriParser.parseFromURI("ircs://irc.test.com:-1");
    }

    @Test(expected = InvalidURIException.class)
    public void testURINotIntPort() throws InvalidURIException {
        final URIParser uriParser = new URIParser();
        uriParser.parseFromURI("ircs://irc.test.com:999999999999");
    }

    @Test(expected = InvalidURIException.class)
    public void testURIPortOutOfRange() throws InvalidURIException {
        final URIParser uriParser = new URIParser();
        uriParser.parseFromURI("ircs://irc.test.com:65536");
    }

    @Test(expected = InvalidURIException.class)
    public void testTextInvalidHostname() throws InvalidURIException {
        final URIParser uriParser = new URIParser();
        uriParser.parseFromText("irc_test.com");
    }

    @Test(expected = InvalidURIException.class)
    public void testTextEmptyPort() throws InvalidURIException {
        final URIParser uriParser = new URIParser();
        uriParser.parseFromText("irc.test.com:");
    }

    @Test(expected = InvalidURIException.class)
    public void testTextInvalidPort() throws InvalidURIException {
        final URIParser uriParser = new URIParser();
        uriParser.parseFromText("irc.test.com:-1");
    }

    @Test(expected = InvalidURIException.class)
    public void testTextNotIntPort() throws InvalidURIException {
        final URIParser uriParser = new URIParser();
        uriParser.parseFromText("irc.test.com:999999999999");
    }

    @Test(expected = InvalidURIException.class)
    public void testTextPortOutOfRange() throws InvalidURIException {
        final URIParser uriParser = new URIParser();
        uriParser.parseFromText("irc.test.com:65536");
    }

}
