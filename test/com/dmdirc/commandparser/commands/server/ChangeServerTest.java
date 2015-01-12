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
package com.dmdirc.commandparser.commands.server;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.ServerCommandContext;
import com.dmdirc.config.profiles.Profile;
import com.dmdirc.events.CommandErrorEvent;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.logger.ErrorManager;
import com.dmdirc.util.URIParser;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyChar;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChangeServerTest {

    @Mock private DMDircMBassador eventBus;
    @Mock private ErrorManager errorManager;
    @Mock private CommandController controller;
    @Mock private FrameContainer tiw;
    @Mock private Profile profile;
    @Mock private Connection connection;
    private ChangeServer command;

    @Before
    public void setUp() {
        when(connection.getProfile()).thenReturn(profile);
        when(tiw.getEventBus()).thenReturn(eventBus);

        command = new ChangeServer(controller, new URIParser());
    }

    @Test
    public void testUsageNoArgs() {
        command.execute(tiw, new CommandArguments(controller, "/server"),
                new ServerCommandContext(null, ChangeServer.INFO, connection));

        verify(tiw).addLine(eq("commandUsage"), anyChar(), anyString(), anyString());
    }

    @Test
    public void testInvalidPort() {
        command.execute(tiw, new CommandArguments(controller, "/server foo:abc"),
                new ServerCommandContext(null, ChangeServer.INFO, connection));

        verify(eventBus).publishAsync(isA(CommandErrorEvent.class));
    }

    @Test
    public void testOutOfRangePort1() {
        command.execute(tiw, new CommandArguments(controller, "/server foo:0"),
                new ServerCommandContext(null, ChangeServer.INFO, connection));

        verify(eventBus).publishAsync(isA(CommandErrorEvent.class));
    }

    @Test
    public void testOutOfRangePort2() {
        command.execute(tiw, new CommandArguments(controller, "/server foo:65537"),
                new ServerCommandContext(null, ChangeServer.INFO, connection));

        verify(eventBus).publishAsync(isA(CommandErrorEvent.class));
    }

    @Test
    public void testExecuteBasic() throws URISyntaxException {
        command.execute(tiw, new CommandArguments(controller, "/server foo:1234"),
                new ServerCommandContext(null, ChangeServer.INFO, connection));

        verify(connection).connect(eq(new URI("irc://foo:1234")), same(profile));
    }

    @Test
    public void testExecuteNoPort() throws URISyntaxException {
        command.execute(tiw, new CommandArguments(controller, "/server foo"),
                new ServerCommandContext(null, ChangeServer.INFO, connection));

        verify(connection).connect(eq(new URI("irc://foo")), same(profile));
    }

    @Test
    public void testExecuteComplex() throws URISyntaxException {
        command.execute(tiw, new CommandArguments(controller, "/server foo:+1234 password"),
                new ServerCommandContext(null, ChangeServer.INFO, connection));

        verify(connection).connect(eq(new URI("ircs://password@foo:1234")), same(profile));
    }

}
