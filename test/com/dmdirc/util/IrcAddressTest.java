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

package com.dmdirc.util;

import com.dmdirc.Main;
import com.dmdirc.ServerManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.addons.ui_dummy.DummyController;
import com.dmdirc.plugins.PluginManager;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class IrcAddressTest {

    @BeforeClass
    public static void setUp() {
        IdentityManager.load();
        Main.setUI(new DummyController());
        Main.ensureExists(PluginManager.getPluginManager(), "tabcompletion");
    }

    @Test(expected=InvalidAddressException.class)
    public void testInvalidProtocol() throws InvalidAddressException {
        new IrcAddress("http://moo!");
    }

    @Test(expected=InvalidAddressException.class)
    public void testNoProtocol() throws InvalidAddressException {
        new IrcAddress("moo!");
    }
    
    @Test(expected=InvalidAddressException.class)
    public void testInvalidURL() throws InvalidAddressException {
        new IrcAddress(":");
    }    

    @Test
    public void testBasic() throws InvalidAddressException {
        final IrcAddress address = new IrcAddress("irc://servername");
        assertEquals("servername", address.getServer());
        assertEquals("", address.getPassword());
        assertEquals(6667, address.getPort());
        assertFalse(address.isSSL());
    }

    @Test
    public void testPasswordSSL() throws InvalidAddressException {
        final IrcAddress address = new IrcAddress("ircs://password@servername");
        assertEquals("servername", address.getServer());
        assertEquals("password", address.getPassword());
        assertEquals(6667, address.getPort());
        assertTrue(address.isSSL());
    }

    @Test
    public void testPort() throws InvalidAddressException {
        final IrcAddress address = new IrcAddress("irc://servername:7000/");
        assertEquals("servername", address.getServer());
        assertEquals("", address.getPassword());
        assertEquals(7000, address.getPort());
        assertFalse(address.isSSL());
    }

    @Test(expected=InvalidAddressException.class)
    public void testInvalidPort() throws InvalidAddressException {
        new IrcAddress("irc://servername:port/");
    }

    @Test
    public void testPortSSL() throws InvalidAddressException {
        final IrcAddress address = new IrcAddress("ircs://servername:+7000/");
        assertEquals("servername", address.getServer());
        assertEquals("", address.getPassword());
        assertEquals(7000, address.getPort());
        assertTrue(address.isSSL());
    }

    @Test
    public void testComplex() throws InvalidAddressException {
        final IrcAddress address = new IrcAddress("ircs://password@servername:+7000/c1,c2,c3");
        assertEquals("servername", address.getServer());
        assertEquals("password", address.getPassword());
        assertEquals(7000, address.getPort());
        assertEquals(3, address.getChannels().size());
        assertTrue(address.isSSL());
    }

    @Test
    public void testConnect() throws InvalidAddressException {
        final IrcAddress address = new IrcAddress("irc://255.255.255.205/a,b,c");

        int initial = ServerManager.getServerManager().numServers();

        address.connect();

        assertEquals(initial + 1, ServerManager.getServerManager().numServers());

        address.connect();

        assertEquals(initial + 1, ServerManager.getServerManager().numServers());
    }

    @Test
    public void testReservedChanNames() throws InvalidAddressException {
        final IrcAddress address1 = new IrcAddress("irc://server/,needpass");
        assertEquals(0, address1.getChannels().size());

        final IrcAddress address2 = new IrcAddress("irc://server/MDbot,needkey");
        assertEquals(1, address2.getChannels().size());
        assertEquals("MDbot", address2.getChannels().get(0));

        final IrcAddress address3 = new IrcAddress("irc://server/MDbot,isnick");
        assertEquals(1, address3.getChannels().size());
        assertEquals("MDbot", address3.getChannels().get(0));
    }

    @Test
    public void testChannels() throws InvalidAddressException {
        final IrcAddress address3 = new IrcAddress("irc://server/#MDbot");
        assertEquals(1, address3.getChannels().size());
        assertEquals("#MDbot", address3.getChannels().get(0));
    }
    
    @Test
    public void testChannelsQuery() throws InvalidAddressException {
        final IrcAddress address3 = new IrcAddress("irc://server/MDbot?moo");
        assertEquals(1, address3.getChannels().size());
        assertEquals("MDbot?moo", address3.getChannels().get(0));
    }    

    @Test
    public void testEncoding() throws InvalidAddressException {
        final IrcAddress address1 = new IrcAddress("irc://server/%23DMDirc");
        assertEquals(1, address1.getChannels().size());
        assertEquals("#DMDirc", address1.getChannels().get(0));
    }

}
