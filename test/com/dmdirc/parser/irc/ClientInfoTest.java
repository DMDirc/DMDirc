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

package com.dmdirc.parser.irc;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class ClientInfoTest {
    
    @Test
    public void testMap() {
        final ClientInfo ci = new ClientInfo(null, "nick!ident@host");
        final Map map = new HashMap();
        
        ci.setMap(map);
        assertEquals(map, ci.getMap());
    }
    
    @Test
    public void testFake() {
        final ClientInfo ci = new ClientInfo(null, "nick!ident@host");
        assertFalse(ci.isFake());
        ci.setFake(true);
        assertTrue(ci.isFake());
        ci.setFake(false);
        assertFalse(ci.isFake());
    }
    
    @Test
    public void testParseHost() {
        final String string1 = ":nick!ident@host";
        final String string2 = "nick";
        final String string3 = ":nick@host";
        
        assertEquals("nick", ClientInfo.parseHost(string1));
        assertEquals("nick", ClientInfo.parseHost(string2));
        assertEquals("nick", ClientInfo.parseHost(string3));
    }
    
    @Test
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
    
    @Test
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
    
    @Test
    public void testToString() {
        final ClientInfo ci = new ClientInfo(null, "nick!ident@host");
        assertEquals("nick!ident@host", ci.toString());
    }
    
    @Test
    public void testAwayState() {
        final ClientInfo ci = new ClientInfo(null, "nick!ident@host");
        assertFalse(ci.getAwayState());
        ci.setAwayState(true);
        assertTrue(ci.getAwayState());
    }
    
    @Test
    public void testAwayReason() {
        final ClientInfo ci = new ClientInfo(null, "nick!ident@host");
        ci.setAwayState(true);
        ci.setAwayReason("away reason");
        
        assertEquals("away reason", ci.getAwayReason());
        ci.setAwayState(false);
        assertEquals("", ci.getAwayReason());
    }
    
    @Test
    public void testRealName() {
        final ClientInfo ci = new ClientInfo(null, "nick!ident@host");
        ci.setRealName("abc def");
        assertEquals("abc def", ci.getRealName());
        ci.setRealName("abc 123 def");
        assertEquals("abc 123 def", ci.getRealName());
    }
    
}
