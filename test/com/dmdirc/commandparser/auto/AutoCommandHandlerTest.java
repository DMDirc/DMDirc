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

package com.dmdirc.commandparser.auto;

import com.dmdirc.FrameContainer;
import com.dmdirc.GlobalWindow;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.config.profiles.Profile;
import com.dmdirc.events.ClientOpenedEvent;
import com.dmdirc.events.ServerConnectedEvent;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AutoCommandHandlerTest {

    @Mock private CommandController commandController;
    @Mock private GlobalCommandParser globalCommandParser;
    @Mock private GlobalWindow globalWindow;
    @Mock private AutoCommand autoCommand;
    @Mock private ClientOpenedEvent clientOpenedEvent;
    @Mock private ServerConnectedEvent serverConnectedEvent;
    @Mock private Connection connection;
    @Mock private Profile profile;
    @Mock private FrameContainer container;
    @Mock private CommandParser commandParser;
    private AutoCommandHandler autoCommandHandler;

    @Before
    public void setup() {
        when(autoCommand.getProfile()).thenReturn(Optional.ofNullable("profile"));
        when(autoCommand.getResponse()).thenReturn("Testing123");
        when(autoCommand.getServer()).thenReturn(Optional.<String>empty());
        when(autoCommand.getNetwork()).thenReturn(Optional.<String>empty());
        when(serverConnectedEvent.getConnection()).thenReturn(connection);
        when(connection.getProfile()).thenReturn(profile);
        when(connection.getWindowModel()).thenReturn(container);
        when(connection.getAddress()).thenReturn("irc.quakenet.org");
        when(connection.getNetwork()).thenReturn("Quakenet");
        when(profile.getName()).thenReturn("profile");
        when(container.getCommandParser()).thenReturn(commandParser);
        autoCommandHandler = new AutoCommandHandler(commandController, globalCommandParser,
                globalWindow, autoCommand);
    }

    @Test
    public void testCheckAutoCommandWithGlobal() {
        autoCommandHandler.checkAutoCommand(clientOpenedEvent);
        verify(globalCommandParser).parseCommand(globalWindow,
                commandController.getCommandChar() + autoCommand.getResponse());
    }

    @Test
    public void testCheckAutoCommandWithoutGlobal() {
        when(autoCommand.getNetwork()).thenReturn(Optional.ofNullable("Quakenet"));
        autoCommandHandler.checkAutoCommand(clientOpenedEvent);
        verify(globalCommandParser, never()).parseCommand(globalWindow,
                commandController.getCommandChar() + autoCommand.getResponse());
    }

    @Test
    public void testCheckAutoCommandNoServerOrNetwork() {
        autoCommandHandler.checkAutoCommand(serverConnectedEvent);
        verify(globalCommandParser, never()).parseCommand(globalWindow,
                commandController.getCommandChar() + autoCommand.getResponse());
        verify(commandParser, never()).parseCommand(container,
                commandController.getCommandChar() + autoCommand.getResponse());
    }

    @Test
    public void testCheckAutoCommandNoProfile() {
        when(autoCommand.getNetwork()).thenReturn(Optional.ofNullable("Quakenet"));
        when(autoCommand.getProfile()).thenReturn(Optional.ofNullable("profile1"));
        autoCommandHandler.checkAutoCommand(serverConnectedEvent);
        verify(globalCommandParser, never()).parseCommand(globalWindow,
                commandController.getCommandChar() + autoCommand.getResponse());
        verify(commandParser, never()).parseCommand(container,
                commandController.getCommandChar() + autoCommand.getResponse());
    }

    @Test
    public void testCheckAutoCommandWithProfile() {
        when(autoCommand.getNetwork()).thenReturn(Optional.ofNullable("Quakenet"));
        autoCommandHandler.checkAutoCommand(serverConnectedEvent);
        verify(globalCommandParser, never()).parseCommand(globalWindow,
                commandController.getCommandChar() + autoCommand.getResponse());
        verify(commandParser, times(1)).parseCommand(container,
                commandController.getCommandChar() + autoCommand.getResponse());
    }

    @Test
    public void testCheckAutoCommandWithProfileNoServer() {
        when(autoCommand.getServer()).thenReturn(Optional.ofNullable("server"));
        autoCommandHandler.checkAutoCommand(serverConnectedEvent);
        verify(globalCommandParser, never()).parseCommand(globalWindow,
                commandController.getCommandChar() + autoCommand.getResponse());
        verify(commandParser, never()).parseCommand(container,
                commandController.getCommandChar() + autoCommand.getResponse());
    }

    @Test
    public void testCheckAutoCommandWithProfileNoServerOrNetwork() {
        when(autoCommand.getNetwork()).thenReturn(Optional.ofNullable("network"));
        autoCommandHandler.checkAutoCommand(serverConnectedEvent);
        verify(globalCommandParser, never()).parseCommand(globalWindow,
                commandController.getCommandChar() + autoCommand.getResponse());
        verify(commandParser, never()).parseCommand(container,
                commandController.getCommandChar() + autoCommand.getResponse());
    }

    @Test
    public void testCheckAutoCommandWithProfileWithServer() {
        when(autoCommand.getNetwork()).thenReturn(Optional.ofNullable("Quakenet"));
        autoCommandHandler.checkAutoCommand(serverConnectedEvent);
        verify(globalCommandParser, never()).parseCommand(globalWindow,
                commandController.getCommandChar() + autoCommand.getResponse());
        verify(commandParser, times(1)).parseCommand(container,
                commandController.getCommandChar() + autoCommand.getResponse());
    }

    @Test
    public void testCheckAutoCommandMultipleLines() {
        when(autoCommand.getResponse()).thenReturn("Testing\n123");
        autoCommandHandler.checkAutoCommand(clientOpenedEvent);
        verify(globalCommandParser, times(1)).parseCommand(globalWindow,
                commandController.getCommandChar() + "Testing");
        verify(globalCommandParser, times(1)).parseCommand(globalWindow,
                commandController.getCommandChar() + "123");
    }
}