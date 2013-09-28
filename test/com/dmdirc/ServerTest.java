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

import com.dmdirc.actions.wrappers.AliasWrapper;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.Identity;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.ui.WindowManager;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class ServerTest {

    @Mock private ServerManager serverManager;
    @Mock private Identity profile;
    @Mock private ConfigManager configManager;
    @Mock private CommandParser commandParser;
    @Mock private ParserFactory parserFactory;
    @Mock private IdentityFactory identityFactory;
    @Mock private WindowManager windowManager;
    @Mock private AliasWrapper aliasWrapper;
    @Mock private CommandController commandController;

    private Server server;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(configManager.getOptionInt(anyString(), anyString()))
                .thenReturn(Integer.MAX_VALUE);

        server = new Server(
                serverManager,
                configManager,
                commandParser,
                parserFactory,
                windowManager,
                aliasWrapper,
                commandController,
                identityFactory,
                new URI("irc-test://255.255.255.255"),
                profile);
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
        server.addInvite(new Invite(server, "#chan1", "a!b@c"));
        server.addInvite(new Invite(server, "#chan1", "d!e@f"));

        assertEquals(1, server.getInvites().size());
    }

    @Test
    public void testRemoveInvites() {
        server.addInvite(new Invite(server, "#chan1", "a!b@c"));
        server.addInvite(new Invite(server, "#chan2", "d!e@f"));

        server.removeInvites("#chan1");
        assertEquals(1, server.getInvites().size());

        server.removeInvites("#chan2");
        assertEquals(0, server.getInvites().size());
    }

}
