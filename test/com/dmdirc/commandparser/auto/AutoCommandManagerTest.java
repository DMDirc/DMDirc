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
import com.dmdirc.GlobalWindow;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.interfaces.CommandController;

import com.google.common.base.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class AutoCommandManagerTest {

    @Mock GlobalCommandParser globalCommandParser;
    @Mock GlobalWindow globalWindow;
    @Mock CommandController commandController;
    @Mock DMDircMBassador eventBus;
    private AutoCommandManager autoCommandManager;

    @Before
    public void setup() {
        final AutoCommandHandlerFactory factory = new AutoCommandHandlerFactory(
                globalCommandParser, globalWindow, commandController);
        autoCommandManager = new AutoCommandManager(eventBus, factory);
    }

    @Test
    public void testRemoveAutoCommand() {
        final AutoCommand global = new AutoCommand(Optional.<String>absent(),
                Optional.<String>absent(), Optional.<String>absent(), "DO STUFF");
        final AutoCommand connection = new AutoCommand(Optional.<String>absent(),
                Optional.fromNullable("Quakenet"), Optional.<String>absent(), "DO STUFF");
        autoCommandManager.addAutoCommand(global);
        autoCommandManager.addAutoCommand(connection);
        assertEquals(2, autoCommandManager.getAutoCommands().size());
        assertTrue(autoCommandManager.getAutoCommands().contains(connection));
        autoCommandManager.removeAutoCommand(connection);
        assertEquals(1, autoCommandManager.getAutoCommands().size());
        assertFalse(autoCommandManager.getAutoCommands().contains(connection));
    }

    @Test
    public void testGetAutoCommands() {
        final AutoCommand global = new AutoCommand(Optional.<String>absent(),
                Optional.<String>absent(), Optional.<String>absent(), "DO STUFF");
        final AutoCommand connection = new AutoCommand(Optional.<String>absent(),
                Optional.fromNullable("Quakenet"), Optional.<String>absent(), "DO STUFF");
        autoCommandManager.addAutoCommand(global);
        autoCommandManager.addAutoCommand(connection);
        assertEquals(2, autoCommandManager.getAutoCommands().size());
    }

    @Test
    public void testGetGlobalAutoCommands() {
        final AutoCommand global = new AutoCommand(Optional.<String>absent(),
                Optional.<String>absent(), Optional.<String>absent(), "DO STUFF");
        final AutoCommand connection = new AutoCommand(Optional.<String>absent(),
                Optional.fromNullable("Quakenet"), Optional.<String>absent(), "DO STUFF");
        autoCommandManager.addAutoCommand(global);
        autoCommandManager.addAutoCommand(connection);
        assertEquals(1, autoCommandManager.getGlobalAutoCommands().size());
        assertTrue(autoCommandManager.getGlobalAutoCommands().contains(global));
    }

    @Test
    public void testGetConnectionAutoCommands() {
        final AutoCommand global = new AutoCommand(Optional.<String>absent(),
                Optional.<String>absent(), Optional.<String>absent(), "DO STUFF");
        final AutoCommand connection = new AutoCommand(Optional.<String>absent(),
                Optional.fromNullable("Quakenet"), Optional.<String>absent(), "DO STUFF");
        autoCommandManager.addAutoCommand(global);
        autoCommandManager.addAutoCommand(connection);
        assertEquals(1, autoCommandManager.getConnectionAutoCommands().size());
        assertTrue(autoCommandManager.getConnectionAutoCommands().contains(connection));
    }
}