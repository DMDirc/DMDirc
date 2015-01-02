/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.config.ConfigBinder;
import com.dmdirc.config.profiles.Profile;
import com.dmdirc.interfaces.User;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.ConfigProviderMigrator;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.input.TabCompleterFactory;
import com.dmdirc.ui.messages.BackBuffer;
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.ui.messages.sink.MessageSinkManager;
import com.dmdirc.util.URLBuilder;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class ServerTest {

    @Mock private Profile profile;
    @Mock private AggregateConfigProvider configManager;
    @Mock private ConfigBinder configBinder;
    @Mock private ConfigProvider userConfig;
    @Mock private ConfigProviderMigrator configMigrator;
    @Mock private CommandParser commandParser;
    @Mock private ParserFactory parserFactory;
    @Mock private IdentityFactory identityFactory;
    @Mock private TabCompleterFactory tabCompleterFactory;
    @Mock private TabCompleter tabCompleter;
    @Mock private MessageSinkManager messageSinkManager;
    @Mock private ChannelFactory channelFactory;
    @Mock private QueryFactory queryFactory;
    @Mock private URLBuilder urlBuilder;
    @Mock private DMDircMBassador eventBus;
    @Mock private ScheduledExecutorService executorService;
    @Mock private MessageEncoderFactory messageEncoderFactory;
    @Mock private BackBufferFactory backBufferFactory;
    @Mock private BackBuffer backBuffer;
    @Mock private UserManager userManager;
    @Mock private User user;

    private Server server;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(configManager.getOptionInt(anyString(), anyString())).thenReturn(Integer.MAX_VALUE);
        when(configManager.getBinder()).thenReturn(configBinder);
        when(configMigrator.getConfigProvider()).thenReturn(configManager);
        when(tabCompleterFactory.getTabCompleter(eq(configManager),
                Matchers.<CommandType>anyVararg())).thenReturn(tabCompleter);
        when(backBufferFactory.getBackBuffer(any())).thenReturn(backBuffer);

        server = new Server(
                configMigrator,
                commandParser,
                parserFactory,
                tabCompleterFactory,
                identityFactory,
                messageSinkManager,
                channelFactory,
                queryFactory,
                urlBuilder,
                eventBus,
                messageEncoderFactory,
                userConfig,
                executorService,
                new URI("irc-test://255.255.255.255"),
                profile,
                backBufferFactory,
                userManager);
    }

    @Test
    public void testGetNetworkFromServerName() {
        final String[][] tests = {
            {"foo.com", "foo.com"},
            {"bar.foo.com", "foo.com"},
            {"irc.us.foo.com", "foo.com"},
            {"irc.foo.co.uk", "foo.co.uk"},
            {"com", "com"},
            {"localhost", "localhost"},
            {"foo.de", "foo.de"}
        };

        for (String[] test : tests) {
            assertEquals(test[1], Server.getNetworkFromServerName(test[0]));
        }
    }

    @Test
    public void testDuplicateInviteRemoval() {
        server.addInvite(new Invite(server, "#chan1", user));
        server.addInvite(new Invite(server, "#chan1", user));

        assertEquals(1, server.getInvites().size());
    }

    @Test
    public void testRemoveInvites() {
        server.addInvite(new Invite(server, "#chan1", user));
        server.addInvite(new Invite(server, "#chan2", user));

        server.removeInvites("#chan1");
        assertEquals(1, server.getInvites().size());

        server.removeInvites("#chan2");
        assertEquals(0, server.getInvites().size());
    }

}
