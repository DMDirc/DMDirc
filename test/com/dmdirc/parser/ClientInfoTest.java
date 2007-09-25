/*
 * ClientInfoTest.java
 * JUnit based test
 *
 * Created on 25 September 2007, 10:35
 */

package com.dmdirc.parser;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author chris
 */
public class ClientInfoTest extends TestCase {
    
    public ClientInfoTest(String testName) {
        super(testName);
    }
    
    public void testMap() {
        final ClientInfo ci = new ClientInfo(null, "nick!ident@host");
        final Map map = new HashMap();
        
        ci.setMap(map);
        assertEquals(map, ci.getMap());
    }
    
    public void testFake() {
        final ClientInfo ci = new ClientInfo(null, "nick!ident@host");
        assertFalse(ci.isFake());
        ci.setFake(true);
        assertTrue(ci.isFake());
        ci.setFake(false);
        assertFalse(ci.isFake());
    }
    
    public void testParseHost() {
        final String string1 = ":nick!ident@host";
        final String string2 = "nick";
        final String string3 = ":nick@host";
        
        assertEquals("nick", ClientInfo.parseHost(string1));
        assertEquals("nick", ClientInfo.parseHost(string2));
        assertEquals("nick", ClientInfo.parseHost(string3));
    }
    
    public void testParseHostFull() {
        final String string1 = ":nick!ident@host";
        final String string2 = "nick";
        final String string3 = ":nick@host";
        
        assertEquals("nick", ClientInfo.parseHostFull(string1)[0]);
        assertEquals("ident", ClientInfo.parseHostFull(string1)[1]);
        assertEquals("host", ClientInfo.parseHostFull(string1)[2]);
        
        assertEquals("nick", ClientInfo.parseHostFull(string2)[0]);
        assertEquals("nick", ClientInfo.parseHostFull(string3)[0]);
        assertEquals("host", ClientInfo.parseHostFull(string3)[2]);
    }
    
    public void testSetUserBits() {
        final ClientInfo ci = new ClientInfo(null, "nick!ident@host");
        ci.setUserBits("nick2!ident2@host2", false);
        
        assertEquals("nick", ci.getNickname());
        assertEquals("ident2", ci.getIdent());
        assertEquals("host2", ci.getHost());
        
        ci.setUserBits(":nick3@host3", true);
        
        assertEquals("nick3", ci.getNickname());
        assertEquals("ident2", ci.getIdent());
        assertEquals("host3", ci.getHost());        
    }
    
    public void testToString() {
        final ClientInfo ci = new ClientInfo(null, "nick!ident@host");
        assertEquals("nick!ident@host", ci.toString());
    }
    
    public void testAwayState() {
        final ClientInfo ci = new ClientInfo(null, "nick!ident@host");
        assertFalse(ci.getAwayState());
        ci.setAwayState(true);
        assertTrue(ci.getAwayState());
    }
    
    public void testAwayReason() {
        final ClientInfo ci = new ClientInfo(null, "nick!ident@host");
        ci.setAwayState(true);
        ci.setAwayReason("away reason");
        
        assertEquals("away reason", ci.getAwayReason());
        ci.setAwayState(false);
        assertEquals("", ci.getAwayReason());
    }
        
    public void testRealName() {
        final ClientInfo ci = new ClientInfo(null, "nick!ident@host");
        ci.setRealName("abc def");
        assertEquals("abc def", ci.getRealName());
        ci.setRealName("abc 123 def");
        assertEquals("abc 123 def", ci.getRealName());        
    }
        
}
