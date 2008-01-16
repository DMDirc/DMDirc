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

import com.dmdirc.Main;
import com.dmdirc.ServerManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.dummy.DummyController;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class IrcAddressTest extends junit.framework.TestCase {
    
    @Before
    public void setUp() {
        IdentityManager.load();
        Main.setUI(new DummyController());
    }    
    
    @Test
    public void testInvalidProtocol() {
        boolean exception = false;
        
        try {
            final IrcAddress address = new IrcAddress("http://moo!");
        } catch (InvalidAddressException ex) {
            exception = true;
        }
        
        assertTrue(exception);
    }
    
    @Test
    public void testNoProtocol() {
        boolean exception = false;
        
        try {
            final IrcAddress address = new IrcAddress("moo!");
        } catch (InvalidAddressException ex) {
            exception = true;
        }
        
        assertTrue(exception);
    }    
    
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
    public void testPort() {
        try {
            final IrcAddress address = new IrcAddress("irc://servername:7000/");
            assertEquals("servername", address.getServer());
            assertEquals("", address.getPassword());
            assertEquals(7000, address.getPort());
            assertFalse(address.isSSL());
        } catch (InvalidAddressException ex) {
            assertFalse(true);
        }
    }
    
    @Test
    public void testInvalidPort() {
        boolean except = false;
        
        try {
            new IrcAddress("irc://servername:port/");
        } catch (InvalidAddressException ex) {
            except = true;
        }
        
        assertTrue(except);
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
    
    @Test
    public void testConnect() {
        try {
            final IrcAddress address = new IrcAddress("irc://255.255.255.205/a,b,c");
            
            int initial = ServerManager.getServerManager().numServers();
            
            address.connect();
            
            assertEquals(initial + 1, ServerManager.getServerManager().numServers());
            
            address.connect();
            
            assertEquals(initial + 1, ServerManager.getServerManager().numServers());
        } catch (InvalidAddressException ex) {
            assertFalse(true);
        }
    }
    
}
