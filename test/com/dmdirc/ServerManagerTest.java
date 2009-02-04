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

package com.dmdirc;

import com.dmdirc.config.IdentityManager;
import com.dmdirc.harness.parser.TestParserFactory;
import com.dmdirc.addons.ui_dummy.DummyController;
import com.dmdirc.addons.ui_dummy.DummyQueryWindow;

import java.util.ArrayList;

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
        final Server server = new Server("255.255.255.255", 6667, "", false,
                IdentityManager.getProfiles().get(0), new ArrayList<String>(),
                new TestParserFactory());
        
        final ServerManager instance = ServerManager.getServerManager();
        
        assertEquals(1, instance.numServers());
        
        server.close();
    }
    
    @Test
    public void testUnregisterServer() {
        final Server server = new Server("255.255.255.255", 6667, "", false,
                IdentityManager.getProfiles().get(0), new ArrayList<String>(),
                new TestParserFactory());
        
        server.close();
        
        final ServerManager instance = ServerManager.getServerManager();
        
        assertEquals(0, instance.numServers());
    }
    
    @Test
    public void testNumServers() {
        final ServerManager instance = ServerManager.getServerManager();
        
        assertEquals(instance.getServers().size(), instance.numServers());
        
        final Server server = new Server("255.255.255.255", 6667, "", false,
                IdentityManager.getProfiles().get(0), new ArrayList<String>(),
                new TestParserFactory());
        
        assertEquals(instance.getServers().size(), instance.numServers());
        
        server.close();
        
        assertEquals(instance.getServers().size(), instance.numServers());
    }
    
    @Test
    public void testGetServerFromFrame() {
        final Server serverA = new Server("255.255.255.255", 6667, "", false,
                IdentityManager.getProfiles().get(0), new ArrayList<String>(),
                new TestParserFactory());
        final Server serverB = new Server("255.255.255.254", 6667, "", false,
                IdentityManager.getProfiles().get(0), new ArrayList<String>(),
                new TestParserFactory());
        
        final ServerManager sm = ServerManager.getServerManager();
        
        assertEquals(serverA, sm.getServerFromFrame(serverA.getFrame()));
        assertEquals(serverB, sm.getServerFromFrame(serverB.getFrame()));
        assertNull(sm.getServerFromFrame(new DummyQueryWindow(serverB)));
        
        serverA.close();
        serverB.close();
    }
    
    @Test
    public void testGetServerByAddress() {
        final Server serverA = new Server("255.255.255.255", 6667, "", false,
                IdentityManager.getProfiles().get(0), new ArrayList<String>(),
                new TestParserFactory());
        final Server serverB = new Server("255.255.255.254", 6667, "", false,
                IdentityManager.getProfiles().get(0), new ArrayList<String>(),
                new TestParserFactory());
        
        final ServerManager sm = ServerManager.getServerManager();
        
        assertEquals(serverA, sm.getServersByAddress("255.255.255.255").get(0));
        assertEquals(serverB, sm.getServersByAddress("255.255.255.254").get(0));
        assertEquals(0, sm.getServersByAddress("255.255.255.253").size());
        
        serverA.close();
        serverB.close();
    }    
    
    @Test
    public void testGetServerByNetwork() throws InterruptedException {
        final Server serverA = new Server("255.255.255.255", 6667, "", false,
                IdentityManager.getProfiles().get(0), new ArrayList<String>(),
                new TestParserFactory("Net1"));
        final Server serverB = new Server("255.255.255.254", 6667, "", false,
                IdentityManager.getProfiles().get(0), new ArrayList<String>(),
                new TestParserFactory("Net2"));
        final Server serverC = new Server("255.255.255.254", 6667, "", false,
                IdentityManager.getProfiles().get(0), new ArrayList<String>(),
                new TestParserFactory("Net2"));
        
        Thread.sleep(1000); // Time for parsers to connect
        
        final ServerManager sm = ServerManager.getServerManager();
        
        assertEquals(1, sm.getServersByNetwork("Net1").size());
        assertEquals(serverA, sm.getServersByNetwork("Net1").get(0));
        
        assertEquals(2, sm.getServersByNetwork("Net2").size());
        assertEquals(serverB, sm.getServersByNetwork("Net2").get(0));
        assertEquals(serverC, sm.getServersByNetwork("Net2").get(1));
        
        assertEquals(0, sm.getServersByAddress("Net3").size());
        
        serverA.close();
        serverB.close();
        serverC.close();
    }
    
}