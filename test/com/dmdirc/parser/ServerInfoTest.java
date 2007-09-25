/*
 * ServerInfoTest.java
 * JUnit based test
 *
 * Created on 25 September 2007, 11:00
 */

package com.dmdirc.parser;

import junit.framework.TestCase;

/**
 *
 * @author chris
 */
public class ServerInfoTest extends TestCase {
    
    public ServerInfoTest(String testName) {
        super(testName);
    }
    
    public void testHost() {
        final ServerInfo si = new ServerInfo("host0", 5, "");
        assertEquals("host0", si.getHost());
        si.setHost("host1");
        assertEquals("host1", si.getHost());
    }
    
    public void testPort() {
        final ServerInfo si = new ServerInfo("host0", 5, "");
        assertEquals(5, si.getPort());
        si.setPort(65530);
        assertEquals(65530, si.getPort());
    }
    
    public void testPassword() {
        final ServerInfo si = new ServerInfo("host0", 5, "pass1");
        assertEquals("pass1", si.getPassword());
        si.setPassword("pass2");
        assertEquals("pass2", si.getPassword());
    }
    
    public void testSSL() {
        final ServerInfo si = new ServerInfo("host0", 5, "pass1");
        assertFalse(si.getSSL());
        si.setSSL(true);
        assertTrue(si.getSSL());
    }
    
    public void testUseSocks() {
        final ServerInfo si = new ServerInfo("host0", 5, "pass1");
        assertFalse(si.getUseSocks());
        si.setUseSocks(true);
        assertTrue(si.getUseSocks());
    }
    
    public void testProxyHost() {
        final ServerInfo si = new ServerInfo("host0", 5, "pass1");
        si.setProxyHost("foo");
        assertEquals("foo", si.getProxyHost());
    }
    
    public void testProxyPort() {
        final ServerInfo si = new ServerInfo("host0", 5, "pass1");
        si.setProxyPort(1024);
        assertEquals(1024, si.getProxyPort());
    }
    
}
