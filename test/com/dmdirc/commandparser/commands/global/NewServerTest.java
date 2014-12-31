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
package com.dmdirc.commandparser.commands.global;

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.config.profiles.Profile;
import com.dmdirc.config.profiles.ProfileManager;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.ConnectionFactory;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.util.URIParser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyChar;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NewServerTest {

    @Mock private CommandController controller;
    @Mock private ProfileManager profileManager;
    @Mock private Profile identity;
    @Mock private FrameContainer container;
    @Mock private PluginManager pluginManager;
    @Mock private ConnectionFactory factory;
    @Mock private Connection connection;
    private NewServer command;

    @Before
    public void setup() {
        when(factory.createServer(any(URI.class), any(Profile.class))).thenReturn(connection);
        when(profileManager.getProfiles()).thenReturn(
                Collections.singletonList(identity));
        command = new NewServer(controller, factory, pluginManager, profileManager, new URIParser());
    }

    @Test
    public void testBasicUsage() throws URISyntaxException {
        command.execute(container, new CommandArguments(controller, "/foo irc.foo.com"),
                new CommandContext(null, NewServer.INFO));

        verify(factory).createServer(eq(new URI("irc://irc.foo.com")), any(Profile.class));
        verify(connection).connect();
    }

    @Test
    public void testPortUsage() throws URISyntaxException {
        command.execute(container, new CommandArguments(controller, "/foo irc.foo.com:1234"),
                new CommandContext(null, NewServer.INFO));

        verify(factory).createServer(eq(new URI("irc://irc.foo.com:1234")), any(Profile.class));
        verify(connection).connect();
    }

    @Test
    public void testUriUsage() throws URISyntaxException {
        command.execute(container, new CommandArguments(controller, "/foo otheruri://foo.com:123/blah"),
                new CommandContext(null, NewServer.INFO));

        verify(factory).createServer(eq(new URI("otheruri://foo.com:123/blah")), any(Profile.class));
        verify(connection).connect();
    }

    @Test
    public void testUsageNoArgs() {
        command.execute(container, new CommandArguments(controller, "/foo"),
                new CommandContext(null, NewServer.INFO));

        verify(container).addLine(eq("commandUsage"), anyChar(), anyString(), anyString());
    }

    @Test
    public void testInvalidPort() {
        command.execute(container, new CommandArguments(controller, "/foo foo:abc"),
                new CommandContext(null, NewServer.INFO));

        verify(container).addLine(eq("commandError"), anyString());
    }

    @Test
    public void testOutOfRangePort1() {
        command.execute(container, new CommandArguments(controller, "/foo foo:0"),
                new CommandContext(null, NewServer.INFO));

        verify(container).addLine(eq("commandError"), anyString());
    }

    @Test
    public void testOutOfRangePort2() {
        command.execute(container, new CommandArguments(controller, "/foo foo:65537"),
                new CommandContext(null, NewServer.INFO));

        verify(container).addLine(eq("commandError"), anyString());
    }

}
