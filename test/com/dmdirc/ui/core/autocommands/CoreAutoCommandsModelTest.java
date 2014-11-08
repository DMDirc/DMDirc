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
import com.dmdirc.commandparser.auto.AutoCommandManager;
import com.dmdirc.interfaces.ui.AutoCommandsModel;
import com.dmdirc.interfaces.ui.AutoCommandsModelListener;

import com.google.common.collect.Sets;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CoreAutoCommandsModelTest {

    @Mock private AutoCommandManager manager;
    @Mock private AutoCommandsModelListener listener;
    private AutoCommandsModel instance;
    private MutableAutoCommand mutableCommand1;
    private MutableAutoCommand mutableCommand2;
    private MutableAutoCommand mutableCommand3;
    private MutableAutoCommand mutableCommand4;
    private MutableAutoCommand mutableCommand5;

    @Before
    public void setUp() throws Exception {
        final AutoCommand command1 = new AutoCommand(Optional.of("server1"),
                Optional.of("network1"), Optional.of("profile1"), "response1");
        final AutoCommand command2 = new AutoCommand(Optional.empty(),
                Optional.empty(), Optional.of("profile2"), "response2");
        final AutoCommand command3 = new AutoCommand(Optional.of("server3"),
                Optional.of("network3"), Optional.of("profile3"), "response3");
        final AutoCommand command4 = new AutoCommand(Optional.empty(),
                Optional.empty(), Optional.of("profile4"), "response4");
        final AutoCommand command5 = new AutoCommand(Optional.empty(),
                Optional.empty(), Optional.of("profile5"), "response5");
        mutableCommand1 = new MutableAutoCommand(command1);
        mutableCommand2 = new MutableAutoCommand(command2);
        mutableCommand3 = new MutableAutoCommand(command3);
        mutableCommand4 = new MutableAutoCommand(command4);
        mutableCommand5 = new MutableAutoCommand(command5);
        when(manager.getAutoCommands())
                .thenReturn(Sets.newHashSet(command1, command2, command3, command4));
        when(manager.getConnectionAutoCommands()).thenReturn(Sets.newHashSet(command1, command3));
        when(manager.getGlobalAutoCommands()).thenReturn(Sets.newHashSet(command2, command4));
        instance = new CoreAutoCommandsModel(manager);
        instance.setType(AutoCommandType.ALL);
        instance.loadModel();
    }

    @Test
    public void testAddListener() throws Exception {
        instance.setSelectedCommand(Optional.of(mutableCommand2));
        verify(listener, never()).setSelectedCommand(Optional.of(mutableCommand2));
        instance.addListener(listener);
        instance.setSelectedCommand(Optional.of(mutableCommand1));
        verify(listener).setSelectedCommand(Optional.of(mutableCommand1));
    }

    @Test
    public void testRemoveListener() throws Exception {
        instance.setSelectedCommand(Optional.of(mutableCommand2));
        verify(listener, never()).setSelectedCommand(Optional.of(mutableCommand2));
        instance.addListener(listener);
        instance.setSelectedCommand(Optional.of(mutableCommand1));
        verify(listener).setSelectedCommand(Optional.of(mutableCommand1));
        instance.removeListener(listener);
        instance.setSelectedCommand(Optional.of(mutableCommand2));
        verify(listener, never()).setSelectedCommand(Optional.of(mutableCommand2));
    }

    @Test
    public void testLoadModel_All() throws Exception {
        instance = new CoreAutoCommandsModel(manager);
        instance.setType(AutoCommandType.ALL);
        assertTrue(instance.getAutoCommands().isEmpty());
        instance.loadModel();
        assertFalse(instance.getAutoCommands().isEmpty());
        assertEquals(4, instance.getAutoCommands().size());
        assertTrue(instance.getAutoCommands().contains(mutableCommand1));
        assertTrue(instance.getAutoCommands().contains(mutableCommand2));
        assertTrue(instance.getAutoCommands().contains(mutableCommand3));
        assertTrue(instance.getAutoCommands().contains(mutableCommand4));
    }

    @Test
    public void testLoadModel_Global() throws Exception {
        instance = new CoreAutoCommandsModel(manager);
        instance.setType(AutoCommandType.GLOBAL);
        assertTrue(instance.getAutoCommands().isEmpty());
        instance.loadModel();
        assertFalse(instance.getAutoCommands().isEmpty());
        assertEquals(2, instance.getAutoCommands().size());
        assertFalse(instance.getAutoCommands().contains(mutableCommand1));
        assertTrue(instance.getAutoCommands().contains(mutableCommand2));
        assertFalse(instance.getAutoCommands().contains(mutableCommand3));
        assertTrue(instance.getAutoCommands().contains(mutableCommand4));
    }

    @Test
    public void testLoadModel_Connection() throws Exception {
        instance = new CoreAutoCommandsModel(manager);
        instance.setType(AutoCommandType.CONNECTION);
        assertTrue(instance.getAutoCommands().isEmpty());
        instance.loadModel();
        assertFalse(instance.getAutoCommands().isEmpty());
        assertEquals(2, instance.getAutoCommands().size());
        assertTrue(instance.getAutoCommands().contains(mutableCommand1));
        assertFalse(instance.getAutoCommands().contains(mutableCommand2));
        assertTrue(instance.getAutoCommands().contains(mutableCommand3));
        assertFalse(instance.getAutoCommands().contains(mutableCommand4));
    }

    @Test
    public void testSetAutoCommands_Empty() throws Exception {
        assertFalse(instance.getAutoCommands().isEmpty());
        instance.setAutoCommands(Sets.newHashSet());
        assertTrue(instance.getAutoCommands().isEmpty());
    }

    @Test
    public void testSetAutoCommands_NotEmpty() throws Exception {
        assertEquals(4, instance.getAutoCommands().size());
        instance.setAutoCommands(Sets.newHashSet(mutableCommand1, mutableCommand2));
        assertEquals(2, instance.getAutoCommands().size());
    }

    @Test
    public void testGetSelectedCommand() throws Exception {
        assertEquals(Optional.<MutableAutoCommand>empty(), instance.getSelectedCommand());
        instance.setSelectedCommand(Optional.of(mutableCommand1));
        assertEquals(Optional.of(mutableCommand1), instance.getSelectedCommand());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSelectedCommand_UnknownCommand() {
        assertEquals(Optional.<MutableAutoCommand>empty(), instance.getSelectedCommand());
        instance.setSelectedCommand(Optional.of(mutableCommand5));
    }

    @Test
    public void testSetSelectedCommand_EmptyCommand() {
        assertEquals(Optional.<MutableAutoCommand>empty(), instance.getSelectedCommand());
        instance.setSelectedCommand(Optional.of(mutableCommand4));
        assertEquals(Optional.of(mutableCommand4), instance.getSelectedCommand());
        instance.setSelectedCommand(Optional.empty());
        assertEquals(Optional.<MutableAutoCommand>empty(), instance.getSelectedCommand());
    }

    @Test
    public void testGetSelectedCommandServer() throws Exception {
        assertEquals(Optional.<String>empty(), instance.getSelectedCommandServer());
        instance.setSelectedCommand(Optional.of(mutableCommand1));
        assertEquals(mutableCommand1.getServer(), instance.getSelectedCommandServer());
    }

    @Test
    public void testGetSelectedCommandNetwork() throws Exception {
        assertEquals(Optional.<String>empty(), instance.getSelectedCommandNetwork());
        instance.setSelectedCommand(Optional.of(mutableCommand1));
        assertEquals(mutableCommand1.getNetwork(), instance.getSelectedCommandNetwork());
    }

    @Test
    public void testGetSelectedCommandProfile() throws Exception {
        assertEquals(Optional.<String>empty(), instance.getSelectedCommandProfile());
        instance.setSelectedCommand(Optional.of(mutableCommand1));
        assertEquals(mutableCommand1.getProfile(), instance.getSelectedCommandProfile());
    }

    @Test
    public void testGetSelectedCommandResponse() throws Exception {
        assertEquals("", instance.getSelectedCommandResponse());
        instance.setSelectedCommand(Optional.of(mutableCommand1));
        assertEquals(mutableCommand1.getResponse(), instance.getSelectedCommandResponse());
    }

    @Test
    public void testSetSelectedCommandServer_Empty() throws Exception {
        assertEquals(Optional.<String>empty(), instance.getSelectedCommandServer());
        instance.setSelectedCommandServer(Optional.of("server"));
        assertEquals(Optional.<String>empty(), instance.getSelectedCommandServer());
    }

    @Test
    public void testSetSelectedCommandServer() throws Exception {
        assertEquals(Optional.<String>empty(), instance.getSelectedCommandServer());
        instance.setSelectedCommand(Optional.of(mutableCommand1));
        instance.setSelectedCommandServer(Optional.of("server"));
        assertEquals(Optional.of("server"), instance.getSelectedCommandServer());
    }

    @Test
    public void testSetSelectedCommandNetwork_Empty() throws Exception {
        assertEquals(Optional.<String>empty(), instance.getSelectedCommandNetwork());
        instance.setSelectedCommandNetwork(Optional.of("network"));
        assertEquals(Optional.<String>empty(), instance.getSelectedCommandNetwork());
    }

    @Test
    public void testSetSelectedCommandNetwork() throws Exception {
        assertEquals(Optional.<String>empty(), instance.getSelectedCommandNetwork());
        instance.setSelectedCommand(Optional.of(mutableCommand1));
        instance.setSelectedCommandNetwork(Optional.of("network"));
        assertEquals(Optional.of("network"), instance.getSelectedCommandNetwork());
    }

    @Test
    public void testSetSelectedCommandProfile_Empty() throws Exception {
        assertEquals(Optional.<String>empty(), instance.getSelectedCommandProfile());
        instance.setSelectedCommandProfile(Optional.of("profile"));
        assertEquals(Optional.<String>empty(), instance.getSelectedCommandProfile());
    }

    @Test
    public void testSetSelectedCommandProfile() throws Exception {
        assertEquals(Optional.<String>empty(), instance.getSelectedCommandProfile());
        instance.setSelectedCommand(Optional.of(mutableCommand1));
        instance.setSelectedCommandProfile(Optional.of("profile"));
        assertEquals(Optional.of("profile"), instance.getSelectedCommandProfile());
    }

    @Test
    public void testSetSelectedCommandResponse_Empty() throws Exception {
        assertEquals("", instance.getSelectedCommandResponse());
        instance.setSelectedCommandResponse("response");
        assertEquals("", instance.getSelectedCommandResponse());
    }

    @Test(expected = NullPointerException.class)
    public void testSetSelectedCommandResponse_Null() throws Exception {
        assertEquals("", instance.getSelectedCommandResponse());
        instance.setSelectedCommandResponse(null);
    }

    @Test
    public void testSetSelectedCommandResponse() throws Exception {
        assertEquals("", instance.getSelectedCommandResponse());
        instance.setSelectedCommand(Optional.of(mutableCommand1));
        instance.setSelectedCommandResponse("response");
        assertEquals("response", instance.getSelectedCommandResponse());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSelectedCommandResponse_EmptyResponse() throws Exception {
        assertEquals("", instance.getSelectedCommandResponse());
        instance.setSelectedCommand(Optional.of(mutableCommand1));
        instance.setSelectedCommandResponse("");
        assertEquals("response", instance.getSelectedCommandResponse());
    }

    @Test
    public void testAddCommand() throws Exception {
        assertEquals(4, instance.getAutoCommands().size());
        assertFalse(instance.getAutoCommands().contains(mutableCommand5));
        instance.addCommand(mutableCommand5);
        assertEquals(5, instance.getAutoCommands().size());
        assertTrue(instance.getAutoCommands().contains(mutableCommand5));
    }

    @Test
    public void testRemoveCommand() throws Exception {
        assertEquals(4, instance.getAutoCommands().size());
        assertTrue(instance.getAutoCommands().contains(mutableCommand4));
        instance.removeCommand(mutableCommand4);
        assertEquals(3, instance.getAutoCommands().size());
        assertFalse(instance.getAutoCommands().contains(mutableCommand4));
    }

    @Test
    public void testSave() throws Exception {
        instance.addCommand(mutableCommand5);
        instance.removeCommand(mutableCommand1);
        instance.save();
        verify(manager).removeAutoCommand(mutableCommand1.getAutoCommand());
        verify(manager).removeAutoCommand(mutableCommand2.getAutoCommand());
        verify(manager).removeAutoCommand(mutableCommand3.getAutoCommand());
        verify(manager).removeAutoCommand(mutableCommand4.getAutoCommand());
        verify(manager, never()).addAutoCommand(mutableCommand1.getAutoCommand());
        verify(manager).addAutoCommand(mutableCommand2.getAutoCommand());
        verify(manager).addAutoCommand(mutableCommand3.getAutoCommand());
        verify(manager).addAutoCommand(mutableCommand4.getAutoCommand());
        verify(manager).addAutoCommand(mutableCommand5.getAutoCommand());
    }
}