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

package com.dmdirc;

import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.dummy.DummyController;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ServerManagerTest extends junit.framework.TestCase {
        
    @Before
    public void setUp() throws Exception {
        Main.setUI(new DummyController());
        IdentityManager.load();
    }
    
    @Test
    public void testGetServerManager() {
        final ServerManager resultA = ServerManager.getServerManager();
        final ServerManager resultB = ServerManager.getServerManager();
        
        assertNotNull(resultA);
        assertTrue(resultA instanceof ServerManager);
        assertEquals(resultA, resultB);
    }
    
    @Test
    public void testRegisterServer() {
        final Server server = new Server("255.255.255.255", 6667, "", false, IdentityManager.getProfiles().get(0));
        
        final ServerManager instance = ServerManager.getServerManager();
        
        assertEquals(1, instance.numServers());
        
        server.close();
    }
    
    @Test
    public void testUnregisterServer() {
        final Server server = new Server("255.255.255.255", 6667, "", false, IdentityManager.getProfiles().get(0));
        
        server.close();
        
        final ServerManager instance = ServerManager.getServerManager();
        
        assertEquals(0, instance.numServers());
    }
    
    @Test
    public void testNumServers() {
        final ServerManager instance = ServerManager.getServerManager();
        
        assertEquals(instance.getServers().size(), instance.numServers());
        
        final Server server = new Server("255.255.255.255", 6667, "", false, IdentityManager.getProfiles().get(0));
        
        assertEquals(instance.getServers().size(), instance.numServers());
        
        server.close();
        
        assertEquals(instance.getServers().size(), instance.numServers());
    }
    
    @Test
    public void testGetServerFromFrame() {
        final Server serverA = new Server("255.255.255.255", 6667, "", false, IdentityManager.getProfiles().get(0));
        final Server serverB = new Server("255.255.255.254", 6667, "", false, IdentityManager.getProfiles().get(0));
        
        final ServerManager sm = ServerManager.getServerManager();
        
        assertEquals(serverA, sm.getServerFromFrame(serverA.getFrame()));
        assertEquals(serverB, sm.getServerFromFrame(serverB.getFrame()));
        
        serverA.close();
        serverB.close();
    }
    
}