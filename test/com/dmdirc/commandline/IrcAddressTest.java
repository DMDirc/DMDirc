/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.commandline;

import org.junit.Test;
import static org.junit.Assert.*;

public class IrcAddressTest extends junit.framework.TestCase {
    
    @Test
    public void testBasic() {
        try {
            final IrcAddress address = new IrcAddress("irc://servername");
            assertEquals("servername", address.getServer());
            assertEquals("", address.getPassword());
            assertEquals(6667, address.getPort());
            assertFalse(address.isSSL());
        } catch (InvalidAddressException ex) {
            assertFalse(true);
        }
    }
    
    @Test
    public void testPasswordSSL() {
        try {
            final IrcAddress address = new IrcAddress("ircs://password@servername");
            assertEquals("servername", address.getServer());
            assertEquals("password", address.getPassword());
            assertEquals(6667, address.getPort());
            assertTrue(address.isSSL());
        } catch (InvalidAddressException ex) {
            assertFalse(true);
        }
    }
    
    @Test
    public void testPortSSL() {
        try {
            final IrcAddress address = new IrcAddress("ircs://servername:+7000/");
            assertEquals("servername", address.getServer());
            assertEquals("", address.getPassword());
            assertEquals(7000, address.getPort());
            assertTrue(address.isSSL());
        } catch (InvalidAddressException ex) {
            assertFalse(true);
        }
    }
    
    @Test
    public void testComplex() {
        try {
            final IrcAddress address = new IrcAddress("ircs://password@servername:+7000/c1,c2,c3");
            assertEquals("servername", address.getServer());
            assertEquals("password", address.getPassword());
            assertEquals(7000, address.getPort());
            assertEquals(3, address.getChannels().size());
            assertTrue(address.isSSL());
        } catch (InvalidAddressException ex) {
            assertFalse(true);
        }
    }
    
}
