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

package com.dmdirc.ui.core.autocommands;

import com.dmdirc.commandparser.auto.AutoCommand;

import java.util.Optional;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;

public class MutableAutoCommandTest {

    private MutableAutoCommand command;
    private final Optional<String> server = Optional.of("server");
    private final Optional<String> network = Optional.of("network");
    private final Optional<String> profile = Optional.of("profile");
    private static final String response = "response";

    @Test
    public void testGetServer() {
        command = new MutableAutoCommand(server, network, profile, response);
        assertEquals("server", command.getServer().get());
    }

    @Test
    public void testGetNetwork() {
        command = new MutableAutoCommand(server, network, profile, response);
        assertEquals("network", command.getNetwork().get());
    }

    @Test
    public void testGetProfile() {
        command = new MutableAutoCommand(server, network, profile, response);
        assertEquals("profile", command.getProfile().get());
    }

    @Test
    public void testGetResponse() {
        command = new MutableAutoCommand(server, network, profile, response);
        assertEquals("response", command.getResponse());
    }

    @Test
    public void testSetServer() {
        command = new MutableAutoCommand(server, network, profile, response);
        assertEquals("server", command.getServer().get());
        command.setServer(Optional.of("newServer"));
        assertEquals("newServer", command.getServer().get());
    }

    @Test
    public void testSetNetwork() {
        command = new MutableAutoCommand(server, network, profile, response);
        assertEquals("network", command.getNetwork().get());
        command.setNetwork(Optional.of("newNetwork"));
        assertEquals("newNetwork", command.getNetwork().get());
    }

    @Test
    public void testSetProfile() {
        command = new MutableAutoCommand(server, network, profile, response);
        assertEquals("profile", command.getProfile().get());
        command.setProfile(Optional.of("newProfile"));
        assertEquals("newProfile", command.getProfile().get());
    }

    @Test
    public void testSetResponse() {
        command = new MutableAutoCommand(server, network, profile, response);
        assertEquals("response", command.getResponse());
        command.setResponse("newResponse");
        assertEquals("newResponse", command.getResponse());
    }

    @Test
    public void testGetAutoCommand() throws Exception {
        command = new MutableAutoCommand(server, network, profile, response);
        final AutoCommand newCommand = command.getAutoCommand();
        assertEquals(command.getServer(), newCommand.getServer());
        assertEquals(command.getNetwork(), newCommand.getNetwork());
        assertEquals(command.getProfile(), newCommand.getProfile());
        assertEquals(command.getResponse(), newCommand.getResponse());
    }

    @Test
    public void testEquals_AutoCommand() {
        final MutableAutoCommand command1 = new MutableAutoCommand(server, network, profile,
                response);
        final AutoCommand command2 = new AutoCommand(server, network, profile, response);
        assertEquals(command1, command2);
    }

    @Test
    public void testEquals_MutableAutoCommand() {
        final MutableAutoCommand command1 = new MutableAutoCommand(server, network, profile,
                response);
        final MutableAutoCommand command2 = new MutableAutoCommand(server, network, profile,
                response);
        assertEquals(command1, command2);
    }

    @Test
    public void testEquals_Null() {
        command = new MutableAutoCommand(server, network, profile, response);
        assertFalse(command.equals(null));
    }

    @Test
    public void testEquals_Random() {
        command = new MutableAutoCommand(server, network, profile, response);
        assertFalse(command.equals(new Object()));
    }
}