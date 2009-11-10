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
import com.dmdirc.addons.ui_dummy.DummyController;
import com.dmdirc.addons.ui_dummy.DummyQueryWindow;
import com.dmdirc.plugins.PluginManager;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ServerManagerTest {
        
    @BeforeClass
    public static void setUp() throws Exception {
        IdentityManager.load();
        Main.setUI(new DummyController());
        Main.ensureExists(PluginManager.getPluginManager(), "tabcompletion");
    }

    @After
    public void tearDown() {
        for (Server server : ServerManager.getServerManager().getServers()) {
            ServerManager.getServerManager().unregisterServer(server);
        }
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
        final Server server = mock(Server.class);
        
        final ServerManager instance = ServerManager.getServerManager();

        instance.registerServer(server);
        
        assertEquals(1, instance.numServers());
    }
    
    @Test
    public void testUnregisterServer() {
        final Server server = mock(Server.class);

        final ServerManager instance = ServerManager.getServerManager();

        instance.registerServer(server);
        instance.unregisterServer(server);
        
        assertEquals(0, instance.numServers());
    }
    
    @Test
    public void testNumServers() {
        final ServerManager instance = ServerManager.getServerManager();
        
        assertEquals(instance.getServers().size(), instance.numServers());
        
        final Server server = mock(Server.class);

        instance.registerServer(server);
        
        assertEquals(instance.getServers().size(), instance.numServers());
        
        instance.unregisterServer(server);
        
        assertEquals(instance.getServers().size(), instance.numServers());
    }
    
    @Test
    public void testGetServerFromFrame() throws URISyntaxException {
        final Server serverA = new Server(new URI("irc-test://255.255.255.255"),
                IdentityManager.getProfiles().get(0));
        final Server serverB = new Server(new URI("irc-test://255.255.255.254"),
                IdentityManager.getProfiles().get(0));
        serverA.connect();
        serverB.connect();
        
        final ServerManager sm = ServerManager.getServerManager();
        
        assertEquals(serverA, sm.getServerFromFrame(serverA.getFrame()));
        assertEquals(serverB, sm.getServerFromFrame(serverB.getFrame()));
        assertNull(sm.getServerFromFrame(new DummyQueryWindow(serverB)));
        
        serverA.close();
        serverB.close();
    }
    
    @Test
    public void testGetServerByAddress() {
        final Server serverA = mock(Server.class);
        final Server serverB = mock(Server.class);
        when(serverA.getName()).thenReturn("255.255.255.255");
        when(serverB.getName()).thenReturn("255.255.255.254");
        
        final ServerManager sm = ServerManager.getServerManager();

        sm.registerServer(serverA);
        sm.registerServer(serverB);
        
        assertEquals(serverA, sm.getServersByAddress("255.255.255.255").get(0));
        assertEquals(serverB, sm.getServersByAddress("255.255.255.254").get(0));
        assertEquals(0, sm.getServersByAddress("255.255.255.253").size());
    }    
    
    @Test
    public void testGetServerByNetwork() throws InterruptedException {
        final Server serverA = mock(Server.class);
        final Server serverB = mock(Server.class);
        final Server serverC = mock(Server.class);

        when(serverA.isNetwork("Net1")).thenReturn(true);
        when(serverB.isNetwork("Net2")).thenReturn(true);
        when(serverC.isNetwork("Net2")).thenReturn(true);
        
        final ServerManager sm = ServerManager.getServerManager();

        sm.registerServer(serverA);
        sm.registerServer(serverB);
        sm.registerServer(serverC);
        
        assertEquals(1, sm.getServersByNetwork("Net1").size());
        assertEquals(serverA, sm.getServersByNetwork("Net1").get(0));
        
        assertEquals(2, sm.getServersByNetwork("Net2").size());
        assertEquals(serverB, sm.getServersByNetwork("Net2").get(0));
        assertEquals(serverC, sm.getServersByNetwork("Net2").get(1));
        
        assertEquals(0, sm.getServersByNetwork("Net3").size());
    }

    @Test
    public void testCloseAll() {
        final Server serverA = mock(Server.class);
        ServerManager.getServerManager().registerServer(serverA);
        ServerManager.getServerManager().closeAll();
        verify(serverA).disconnect();
        verify(serverA).close();
    }

    @Test
    public void testCloseAllWithMessage() {
        final Server serverA = mock(Server.class);
        ServerManager.getServerManager().registerServer(serverA);
        ServerManager.getServerManager().closeAll("message here");
        verify(serverA).disconnect("message here");
        verify(serverA).close();
    }

    @Test
    public void testDisconnectAll() {
        final Server serverA = mock(Server.class);
        ServerManager.getServerManager().registerServer(serverA);
        ServerManager.getServerManager().disconnectAll("message here");
        verify(serverA).disconnect("message here");
    }

    @Test
    public void testDevChatWithChannel() {
        final Server serverA = mock(Server.class);
        when(serverA.isNetwork("Quakenet")).thenReturn(true);
        when(serverA.hasChannel("#DMDirc")).thenReturn(true);
        when(serverA.getState()).thenReturn(ServerState.CONNECTED);

        ServerManager.getServerManager().registerServer(serverA);
        ServerManager.getServerManager().joinDevChat();

        verify(serverA).join("#DMDirc");
    }

    @Test
    public void testDevChatWithoutChannel() {
        final Server serverA = mock(Server.class);
        when(serverA.isNetwork("Quakenet")).thenReturn(true);
        when(serverA.hasChannel("#DMDirc")).thenReturn(false);
        when(serverA.getState()).thenReturn(ServerState.CONNECTED);

        ServerManager.getServerManager().registerServer(serverA);
        ServerManager.getServerManager().joinDevChat();

        verify(serverA).join("#DMDirc");
    }

    @Test
    public void testDevChatNoServers() {
        final Server serverA = mock(Server.class);
        when(serverA.isNetwork("Quakenet")).thenReturn(true);
        when(serverA.getState()).thenReturn(ServerState.DISCONNECTING);

        final Server serverB = mock(Server.class);
        when(serverB.getNetwork()).thenReturn("Foonet");
        when(serverB.getState()).thenReturn(ServerState.CONNECTED);

        ServerManager.getServerManager().registerServer(serverA);
        ServerManager.getServerManager().registerServer(serverB);

        ServerManager.getServerManager().joinDevChat();

        assertEquals(3, ServerManager.getServerManager().numServers());
    }
    
}