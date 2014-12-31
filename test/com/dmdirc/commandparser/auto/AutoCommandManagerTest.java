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

package com.dmdirc.commandparser.auto;

import com.dmdirc.DMDircMBassador;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AutoCommandManagerTest {

    @Mock private DMDircMBassador eventBus;
    @Mock private AutoCommandHandlerFactory factory;
    @Mock private AutoCommandHandler globalHandler;
    @Mock private AutoCommandHandler ircquakenetHandler;
    @Mock private AutoCommandHandler ukquakenetHandler;
    @Mock private AutoCommandHandler testnetHandler;
    private AutoCommandManager autoCommandManager;
    private AutoCommand global;
    private AutoCommand ircquakenet;
    private AutoCommand quakenet;
    private AutoCommand testnet;

    @Before
    public void setup() {
        autoCommandManager = new AutoCommandManager(eventBus, factory);
        global = AutoCommand.create(Optional.<String>empty(), Optional.<String>empty(),
                Optional.<String>empty(), "");
        ircquakenet = AutoCommand.create(Optional.ofNullable("irc.quakenet.org"),
                Optional.ofNullable("Quakenet"), Optional.<String>empty(), "");
        quakenet = AutoCommand.create(Optional.empty(),
                Optional.ofNullable("Quakenet"), Optional.<String>empty(), "");
        testnet = AutoCommand.create(Optional.ofNullable("irc.testnet.org"),
                Optional.ofNullable("Testnet"), Optional.ofNullable("profileName"), "");
        when(factory.getAutoCommandHandler(global)).thenReturn(globalHandler);
        when(factory.getAutoCommandHandler(ircquakenet)).thenReturn(ircquakenetHandler);
        when(factory.getAutoCommandHandler(quakenet)).thenReturn(ukquakenetHandler);
        when(factory.getAutoCommandHandler(testnet)).thenReturn(testnetHandler);
        autoCommandManager.addAutoCommand(global);
        autoCommandManager.addAutoCommand(ircquakenet);
        autoCommandManager.addAutoCommand(quakenet);
        autoCommandManager.addAutoCommand(testnet);
    }

    @Test
    public void testRemoveAutoCommand() {
        assertEquals(4, autoCommandManager.getAutoCommands().size());
        assertTrue(autoCommandManager.getAutoCommands().contains(ircquakenet));
        autoCommandManager.removeAutoCommand(ircquakenet);
        assertEquals(3, autoCommandManager.getAutoCommands().size());
        assertFalse(autoCommandManager.getAutoCommands().contains(ircquakenet));
    }

    @Test
    public void testGetAutoCommands() {
        assertEquals(4, autoCommandManager.getAutoCommands().size());
    }

    @Test
    public void testGetGlobalAutoCommands() {
        assertTrue(autoCommandManager.getGlobalAutoCommand().isPresent());
        assertEquals(global, autoCommandManager.getGlobalAutoCommand().get());
    }

    @Test(expected = IllegalStateException.class)
    public void testAddMultipleGlobalCommands() {
        autoCommandManager.addAutoCommand(global);
    }

    @Test(expected = IllegalStateException.class)
    public void testAddMultipleNormalCommands() {
        autoCommandManager.addAutoCommand(quakenet);
    }

    @Test
    public void testGetAutoCommandWithNoProfile() {
        final Optional<AutoCommand> autoCommand = autoCommandManager.getAutoCommand(
                Optional.of("Quakenet"), Optional.of("irc.quakenet.org"), Optional.empty());
        assertTrue(autoCommand.isPresent());
        assertEquals(ircquakenet, autoCommand.get());
    }

    @Test
    public void testGetAutoCommandWithOnlyNetwork() {
        final Optional<AutoCommand> autoCommand = autoCommandManager.getAutoCommand(
                Optional.of("Quakenet"), Optional.empty(), Optional.empty());
        assertTrue(autoCommand.isPresent());
        assertEquals(quakenet, autoCommand.get());
    }

    @Test
    public void testGetAutoCommandWithNoMatches() {
        final Optional<AutoCommand> autoCommand = autoCommandManager.getAutoCommand(
                Optional.empty(), Optional.of("irc.quakenet.org"), Optional.empty());
        assertFalse(autoCommand.isPresent());
    }


    @Test
    public void testGetAutoCommandWithAllFields() {
        final Optional<AutoCommand> autoCommand = autoCommandManager.getAutoCommand(
                Optional.of("Testnet"), Optional.of("irc.testnet.org"), Optional.of("profileName"));
        assertTrue(autoCommand.isPresent());
        assertEquals(testnet, autoCommand.get());
    }

    @Test
    public void testGetOrCreateAutoCommandWithExisting() {
        final AutoCommand autoCommand = autoCommandManager.getOrCreateAutoCommand(
                Optional.of("Quakenet"), Optional.empty(), Optional.empty());
        assertEquals(quakenet, autoCommand);
    }

    @Test
    public void testGetOrCreateAutoCommandWithNoMatches() {
        final AutoCommand autoCommand = autoCommandManager.getOrCreateAutoCommand(
                Optional.empty(), Optional.of("irc.quakenet.org"), Optional.empty());

        // It isn't an existing one...
        assertNotEquals(global, autoCommand);
        assertNotEquals(quakenet, autoCommand);
        assertNotEquals(ircquakenet, autoCommand);
        assertNotEquals(testnet, autoCommand);

        // And it has the right targets
        assertEquals(Optional.<String>empty(), autoCommand.getNetwork());
        assertEquals(Optional.of("irc.quakenet.org"), autoCommand.getServer());
        assertEquals(Optional.<String>empty(), autoCommand.getProfile());
    }

    @Test
    public void testStart() {
        autoCommandManager.start();
        verify(eventBus).subscribe(globalHandler);
    }

    @Test
    public void testStop() {
        autoCommandManager.stop();
        verify(eventBus).unsubscribe(globalHandler);
    }

    @Test
    public void testAddWhenStarted() {
        autoCommandManager.removeAutoCommand(global);
        autoCommandManager.start();
        autoCommandManager.addAutoCommand(global);
        verify(eventBus).subscribe(globalHandler);
    }

    @Test
    public void testAddWhenStopped() {
        verify(eventBus, never()).subscribe(globalHandler);
    }

    @Test
    public void testRemoveWhenStarted() {
        autoCommandManager.start();
        autoCommandManager.removeAutoCommand(global);
        verify(eventBus).subscribe(globalHandler);
        verify(eventBus).unsubscribe(globalHandler);
    }

    @Test
    public void testRemoveWhenStopped() {
        autoCommandManager.removeAutoCommand(global);
        verify(eventBus, never()).subscribe(global);
        verify(eventBus, never()).unsubscribe(globalHandler);
    }
}