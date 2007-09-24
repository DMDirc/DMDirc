/*
 * ProgramErrorTest.java
 * JUnit based test
 *
 * Created on 08 April 2007, 13:20
 */

package com.dmdirc.commandline;

import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.*;

public class IrcAddressTest extends TestCase {
    
    public IrcAddressTest(String testName) {
        super(testName);
    }
    
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
