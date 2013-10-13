/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.messages.MessageSinkManager;
import com.dmdirc.parser.common.ChannelJoinRequest;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.input.TabCompleterFactory;

import javax.inject.Provider;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ServerManagerTest {

    @Mock private IdentityController identityController;
    @Mock private IdentityFactory identityFactory;
    @Mock private Provider<CommandController> commandControllerProvider;
    @Mock private WindowManager windowManager;
    @Mock private ServerFactoryImpl serverFactoryImpl;
    private ServerManager serverManager;

    @Before
    public void setUp() throws Exception {
        serverManager = new ServerManager(identityController, identityFactory,
                commandControllerProvider, windowManager, serverFactoryImpl);
    }

    @After
    public void tearDown() {
        for (Server server : serverManager.getServers()) {
            serverManager.unregisterServer(server);
        }
    }

    @Test
    public void testRegisterServer() {
        final Server server = mock(Server.class);
        serverManager.registerServer(server);
        assertEquals(1, serverManager.numServers());
    }

    @Test
    public void testUnregisterServer() {
        final Server server = mock(Server.class);
        serverManager.registerServer(server);
        serverManager.unregisterServer(server);
        assertEquals(0, serverManager.numServers());
    }

    @Test
    public void testNumServers() {
        assertEquals(serverManager.getServers().size(), serverManager.numServers());
        final Server server = mock(Server.class);
        serverManager.registerServer(server);
        assertEquals(serverManager.getServers().size(), serverManager.numServers());
        serverManager.unregisterServer(server);
        assertEquals(serverManager.getServers().size(), serverManager.numServers());
    }

    @Test
    public void testGetServerByAddress() {
        final Server serverA = mock(Server.class);
        final Server serverB = mock(Server.class);
        when(serverA.getAddress()).thenReturn("255.255.255.255");
        when(serverB.getAddress()).thenReturn("255.255.255.254");

        serverManager.registerServer(serverA);
        serverManager.registerServer(serverB);

        assertEquals(serverA, serverManager.getServersByAddress("255.255.255.255").get(0));
        assertEquals(serverB, serverManager.getServersByAddress("255.255.255.254").get(0));
        assertEquals(0, serverManager.getServersByAddress("255.255.255.253").size());
    }

    @Test
    public void testGetServerByNetwork() throws InterruptedException {
        final Server serverA = mock(Server.class);
        final Server serverB = mock(Server.class);
        final Server serverC = mock(Server.class);

        when(serverA.isNetwork("Net1")).thenReturn(true);
        when(serverB.isNetwork("Net2")).thenReturn(true);
        when(serverC.isNetwork("Net2")).thenReturn(true);

        serverManager.registerServer(serverA);
        serverManager.registerServer(serverB);
        serverManager.registerServer(serverC);

        assertEquals(1, serverManager.getServersByNetwork("Net1").size());
        assertEquals(serverA, serverManager.getServersByNetwork("Net1").get(0));

        assertEquals(2, serverManager.getServersByNetwork("Net2").size());
        assertEquals(serverB, serverManager.getServersByNetwork("Net2").get(0));
        assertEquals(serverC, serverManager.getServersByNetwork("Net2").get(1));

        assertEquals(0, serverManager.getServersByNetwork("Net3").size());
    }

    @Test
    public void testCloseAll() {
        final Server serverA = mock(Server.class);
        serverManager.registerServer(serverA);
        serverManager.closeAll();
        verify(serverA).disconnect();
        verify(serverA).close();
    }

    @Test
    public void testCloseAllWithMessage() {
        final Server serverA = mock(Server.class);
        serverManager.registerServer(serverA);
        serverManager.closeAll("message here");
        verify(serverA).disconnect("message here");
        verify(serverA).close();
    }

    @Test
    public void testDisconnectAll() {
        final Server serverA = mock(Server.class);
        serverManager.registerServer(serverA);
        serverManager.disconnectAll("message here");
        verify(serverA).disconnect("message here");
    }

    @Test
    public void testDevChatWithChannel() {
        final Server serverA = mock(Server.class);
        when(serverA.isNetwork("Quakenet")).thenReturn(true);
        when(serverA.hasChannel("#DMDirc")).thenReturn(true);
        when(serverA.getState()).thenReturn(ServerState.CONNECTED);

        serverManager.registerServer(serverA);
        serverManager.joinDevChat();

        verify(serverA).join(new ChannelJoinRequest("#DMDirc"));
    }

    @Test
    public void testDevChatWithoutChannel() {
        final Server serverA = mock(Server.class);
        when(serverA.isNetwork("Quakenet")).thenReturn(true);
        when(serverA.hasChannel("#DMDirc")).thenReturn(false);
        when(serverA.getState()).thenReturn(ServerState.CONNECTED);

        serverManager.registerServer(serverA);
        serverManager.joinDevChat();

        verify(serverA).join(new ChannelJoinRequest("#DMDirc"));
    }

    @Test
    @Ignore("Doesn't work in a headless environment (causes a new server to initialise an IRCDocument)")
    public void testDevChatNoServers() {
        final Server serverA = mock(Server.class);
        when(serverA.isNetwork("Quakenet")).thenReturn(true);
        when(serverA.getState()).thenReturn(ServerState.DISCONNECTING);

        final Server serverB = mock(Server.class);
        when(serverB.getNetwork()).thenReturn("Foonet");
        when(serverB.getState()).thenReturn(ServerState.CONNECTED);

        serverManager.registerServer(serverA);
        serverManager.registerServer(serverB);

        serverManager.joinDevChat();

        assertEquals(3, serverManager.numServers());
    }

}