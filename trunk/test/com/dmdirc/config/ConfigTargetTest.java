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

package com.dmdirc.config;

import org.junit.Test;
import static org.junit.Assert.*;

public class ConfigTargetTest extends junit.framework.TestCase {
    
    @Test
    public void testDefault() {
        final ConfigTarget target = new ConfigTarget();
        
        assertEquals(target.getType(), ConfigTarget.TYPE.GLOBAL);
    }

    @Test
    public void testSetGlobal() {
        final ConfigTarget target = new ConfigTarget();
        target.setGlobal();
        
        assertEquals(target.getType(), ConfigTarget.TYPE.GLOBAL);
        assertEquals(target.getTypeName(), "global");
    }
    
    @Test
    public void testSetGlobalDefault() {
        final ConfigTarget target = new ConfigTarget();
        target.setGlobalDefault();
        
        assertEquals(target.getType(), ConfigTarget.TYPE.GLOBALDEFAULT);
        assertEquals(target.getTypeName(), "globaldefault");
    }
    
    @Test
    public void testSetTheme() {
        final ConfigTarget target = new ConfigTarget();
        target.setTheme();
        
        assertEquals(target.getType(), ConfigTarget.TYPE.THEME);
        assertEquals(target.getTypeName(), "theme");
    }
    
    @Test
    public void testSetProfile() {
        final ConfigTarget target = new ConfigTarget();
        target.setProfile();
        
        assertEquals(target.getType(), ConfigTarget.TYPE.PROFILE);
        assertEquals(target.getTypeName(), "profile");
    }
    
    @Test
    public void testSetIrcd() {
        final ConfigTarget target = new ConfigTarget();
        target.setIrcd("ircd_name");
        
        assertEquals(target.getType(), ConfigTarget.TYPE.IRCD);
        assertEquals(target.getTypeName(), "ircd");
        assertEquals(target.getData(), "ircd_name");
    }
    
    @Test
    public void testSetNetwork() {
        final ConfigTarget target = new ConfigTarget();
        target.setNetwork("net_name");
        
        assertEquals(target.getType(), ConfigTarget.TYPE.NETWORK);
        assertEquals(target.getTypeName(), "network");
        assertEquals(target.getData(), "net_name");        
    }
    
    @Test
    public void testSetServer() {
        final ConfigTarget target = new ConfigTarget();
        target.setServer("server_name");
        
        assertEquals(target.getType(), ConfigTarget.TYPE.SERVER);
        assertEquals(target.getTypeName(), "server");
        assertEquals(target.getData(), "server_name");        
    }
    
    @Test
    public void testSetChannel() {
        final ConfigTarget target = new ConfigTarget();
        target.setChannel("channel_name");
        
        assertEquals(target.getType(), ConfigTarget.TYPE.CHANNEL);
        assertEquals(target.getTypeName(), "channel");
        assertEquals(target.getData(), "channel_name");        
    }
       
}
