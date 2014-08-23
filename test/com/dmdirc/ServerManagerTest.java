/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.ConfigProviderMigrator;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.parser.common.ChannelJoinRequest;
import com.dmdirc.ui.WindowManager;

import net.engio.mbassy.bus.MBassador;

import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServerManagerTest {

    @Mock private IdentityController identityController;
    @Mock private IdentityFactory identityFactory;
    @Mock private ConfigProviderMigrator configProviderMigrator;
    @Mock private ConfigProvider profile;
    @Mock private AggregateConfigProvider configProvider;
    @Mock private Provider<CommandController> commandControllerProvider;
    @Mock private CommandController commandController;
    @Mock private WindowManager windowManager;
    @Mock private ServerFactoryImpl serverFactoryImpl;
    @Mock private Server server;
    @Mock private MBassador eventBus;

    @Captor private ArgumentCaptor<URI> uriCaptor;

    private ServerManager serverManager;

    @Before
    public void setUp() throws Exception {
        serverManager = new ServerManager(identityController, identityFactory,
                commandControllerProvider, windowManager, serverFactoryImpl, eventBus);

        when(commandControllerProvider.get()).thenReturn(commandController);

        when(identityController.getProvidersByType("profile")).thenReturn(Arrays.asList(profile));
        when(identityFactory.createMigratableConfig(anyString(), anyString(), anyString(),
                anyString())).thenReturn(configProviderMigrator);
        when(configProviderMigrator.getConfigProvider()).thenReturn(configProvider);
        when(profile.isProfile()).thenReturn(true);

        when(serverFactoryImpl.getServer(eq(configProviderMigrator), any(CommandParser.class),
                any(ScheduledExecutorService.class), uriCaptor.capture(), eq(profile)))
                .thenReturn(server);
    }

    @Test
    public void testRegisterServer() {
        serverManager.registerServer(server);
        assertEquals(1, serverManager.numServers());
    }

    @Test
    public void testUnregisterServer() {
        serverManager.registerServer(server);
        serverManager.unregisterServer(server);
        assertEquals(0, serverManager.numServers());
    }

    @Test
    public void testNumServers() {
        assertEquals(serverManager.getServers().size(), serverManager.numServers());
        serverManager.registerServer(server);
        assertEquals(serverManager.getServers().size(), serverManager.numServers());
        serverManager.unregisterServer(server);
        assertEquals(serverManager.getServers().size(), serverManager.numServers());
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
    public void testDevChatNoServers() {
        final Server serverA = mock(Server.class);
        when(serverA.isNetwork("Quakenet")).thenReturn(true);
        when(serverA.getState()).thenReturn(ServerState.DISCONNECTING);

        final Server serverB = mock(Server.class);
        when(serverB.isNetwork("Quakenet")).thenReturn(false);
        when(serverB.getState()).thenReturn(ServerState.CONNECTED);

        serverManager.registerServer(serverA);
        serverManager.registerServer(serverB);

        serverManager.joinDevChat();

        assertEquals(3, serverManager.numServers());

        URI serverUri = uriCaptor.getValue();
        assertEquals("irc", serverUri.getScheme());
        assertEquals("irc.quakenet.org", serverUri.getHost());
        assertEquals("DMDirc", serverUri.getPath().substring(1));
    }

    @Test
    public void testAddsNewServersToWindowManager() {
        final Server newServer = serverManager.connectToAddress(URI.create("irc://fobar"));
        verify(windowManager).addWindow(newServer);
    }

    @Test
    public void testAddsRawWindows() {
        when(configProvider.getOptionBool("general", "showrawwindow")).thenReturn(true);
        serverManager.connectToAddress(URI.create("irc://fobar"));
        verify(server).addRaw();
    }

    @Test
    public void testDoesNotAddRawWindows() {
        when(configProvider.getOptionBool("general", "showrawwindow")).thenReturn(false);
        serverManager.connectToAddress(URI.create("irc://fobar"));
        verify(server, never()).addRaw();
    }

}
