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
package com.dmdirc.commandparser.commands.global;

import com.dmdirc.FrameContainer;
import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.ServerFactory;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.util.URIParser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NewServerTest {

    @Mock private CommandController controller;
    @Mock private IdentityController identityController;
    @Mock private ConfigProvider identity;
    @Mock private FrameContainer container;
    @Mock private PluginManager pluginManager;
    @Mock private ServerFactory factory;
    @Mock private Server server;
    private NewServer command;

    @Before
    public void setup() {
        when(factory.createServer(any(URI.class), any(ConfigProvider.class))).thenReturn(server);
        when(identityController.getProvidersByType("profile")).thenReturn(Arrays.asList(new ConfigProvider[] { identity }));
        command = new NewServer(controller, factory, pluginManager, identityController, new URIParser());
    }

    @Test
    public void testBasicUsage() throws URISyntaxException {
        command.execute(container, new CommandArguments(controller, "/foo irc.foo.com"),
                new CommandContext(null, NewServer.INFO));

        verify(factory).createServer(eq(new URI("irc://irc.foo.com:6667")), any(ConfigProvider.class));
        verify(server).connect();
    }

    @Test
    public void testPortUsage() throws URISyntaxException {
        command.execute(container, new CommandArguments(controller, "/foo irc.foo.com:1234"),
                new CommandContext(null, NewServer.INFO));

        verify(factory).createServer(eq(new URI("irc://irc.foo.com:1234")), any(ConfigProvider.class));
        verify(server).connect();
    }

    @Test
    public void testUriUsage() throws URISyntaxException {
        command.execute(container, new CommandArguments(controller, "/foo otheruri://foo.com:123/blah"),
                new CommandContext(null, NewServer.INFO));

        verify(factory).createServer(eq(new URI("otheruri://foo.com:123/blah")), any(ConfigProvider.class));
        verify(server).connect();
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
