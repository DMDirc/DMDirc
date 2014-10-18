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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AutoCommandManagerTest {

    @Mock private DMDircMBassador eventBus;
    @Mock private AutoCommandHandlerFactory factory;
    @Mock private AutoCommandHandler globalHandler;
    @Mock private AutoCommandHandler connectionHandler;
    private AutoCommandManager autoCommandManager;
    private AutoCommand global;
    private AutoCommand connection;

    @Before
    public void setup() {
        autoCommandManager = new AutoCommandManager(eventBus, factory);
        global = new AutoCommand(Optional.<String>empty(), Optional.<String>empty(),
                Optional.<String>empty(), "");
        connection = new AutoCommand(Optional.<String>empty(),
                Optional.ofNullable("Quakenet"), Optional.<String>empty(), "");
        when(factory.getAutoCommandHandler(global)).thenReturn(globalHandler);
        when(factory.getAutoCommandHandler(connection)).thenReturn(connectionHandler);
    }

    @Test
    public void testRemoveAutoCommand() {
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
        autoCommandManager.addAutoCommand(global);
        autoCommandManager.addAutoCommand(connection);
        assertEquals(2, autoCommandManager.getAutoCommands().size());
    }

    @Test
    public void testGetGlobalAutoCommands() {
        autoCommandManager.addAutoCommand(global);
        autoCommandManager.addAutoCommand(connection);
        assertEquals(1, autoCommandManager.getGlobalAutoCommands().size());
        assertTrue(autoCommandManager.getGlobalAutoCommands().contains(global));
    }

    @Test
    public void testGetConnectionAutoCommands() {
        autoCommandManager.addAutoCommand(global);
        autoCommandManager.addAutoCommand(connection);
        assertEquals(1, autoCommandManager.getConnectionAutoCommands().size());
        assertTrue(autoCommandManager.getConnectionAutoCommands().contains(connection));
    }

    @Test
    public void testStart() {
        autoCommandManager.addAutoCommand(global);
        autoCommandManager.start();
        verify(eventBus).subscribe(globalHandler);
    }

    @Test
    public void testStop() {
        autoCommandManager.addAutoCommand(global);
        autoCommandManager.stop();
        verify(eventBus).unsubscribe(globalHandler);
    }

    @Test
    public void testAddWhenStarted() {
        autoCommandManager.start();
        autoCommandManager.addAutoCommand(global);
        verify(eventBus).subscribe(globalHandler);
    }

    @Test
    public void testAddWhenStopped() {
        autoCommandManager.addAutoCommand(global);
        verify(eventBus, never()).subscribe(globalHandler);
    }

    @Test
    public void testRemoveWhenStarted() {
        autoCommandManager.start();
        autoCommandManager.addAutoCommand(global);
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