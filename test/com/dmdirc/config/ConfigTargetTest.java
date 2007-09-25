/*
 * ConfigTargetTest.java
 * JUnit based test
 *
 * Created on 25 September 2007, 10:01
 */

package com.dmdirc.config;

import junit.framework.TestCase;

/**
 *
 * @author chris
 */
public class ConfigTargetTest extends TestCase {
    
    public ConfigTargetTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testDefault() {
        final ConfigTarget target = new ConfigTarget();
        
        assertEquals(target.getType(), ConfigTarget.TYPE_GLOBAL);
    }
    
    public void testSetGlobal() {
        final ConfigTarget target = new ConfigTarget();
        target.setGlobal();
        
        assertEquals(target.getType(), ConfigTarget.TYPE_GLOBAL);
        assertEquals(target.getTypeName(), "global");
    }
    
    public void testSetGlobalDefault() {
        final ConfigTarget target = new ConfigTarget();
        target.setGlobalDefault();
        
        assertEquals(target.getType(), ConfigTarget.TYPE_GLOBALDEFAULT);
        assertEquals(target.getTypeName(), "globaldefault");
    }
    
    public void testSetTheme() {
        final ConfigTarget target = new ConfigTarget();
        target.setTheme();
        
        assertEquals(target.getType(), ConfigTarget.TYPE_THEME);
        assertEquals(target.getTypeName(), "theme");
    }
    
    public void testSetProfile() {
        final ConfigTarget target = new ConfigTarget();
        target.setProfile();
        
        assertEquals(target.getType(), ConfigTarget.TYPE_PROFILE);
        assertEquals(target.getTypeName(), "profile");
    }
    
    public void testSetIrcd() {
        final ConfigTarget target = new ConfigTarget();
        target.setIrcd("ircd_name");
        
        assertEquals(target.getType(), ConfigTarget.TYPE_IRCD);
        assertEquals(target.getTypeName(), "ircd");
        assertEquals(target.getData(), "ircd_name");
    }
    
    public void testSetNetwork() {
        final ConfigTarget target = new ConfigTarget();
        target.setNetwork("net_name");
        
        assertEquals(target.getType(), ConfigTarget.TYPE_NETWORK);
        assertEquals(target.getTypeName(), "network");
        assertEquals(target.getData(), "net_name");        
    }
    
    public void testSetServer() {
        final ConfigTarget target = new ConfigTarget();
        target.setServer("server_name");
        
        assertEquals(target.getType(), ConfigTarget.TYPE_SERVER);
        assertEquals(target.getTypeName(), "server");
        assertEquals(target.getData(), "server_name");        
    }
    
    public void testSetChannel() {
        final ConfigTarget target = new ConfigTarget();
        target.setChannel("channel_name");
        
        assertEquals(target.getType(), ConfigTarget.TYPE_CHANNEL);
        assertEquals(target.getTypeName(), "channel");
        assertEquals(target.getData(), "channel_name");        
    }
       
}
