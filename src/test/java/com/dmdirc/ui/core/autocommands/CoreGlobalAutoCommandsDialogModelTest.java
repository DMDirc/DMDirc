/*
 * Copyright (c) 2006-2017 DMDirc Developers
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
import com.dmdirc.interfaces.CommandController;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CoreGlobalAutoCommandsDialogModelTest {

    @Mock private AutoCommandManager autoCommandManager;
    @Mock private CommandController commandController;
    @Mock private AutoCommand autoCommand;
    private CoreGlobalAutoCommandsDialogModel instance;

    @Before
    public void setUp() throws Exception {
        when(autoCommand.getServer()).thenReturn(Optional.empty());
        when(autoCommand.getNetwork()).thenReturn(Optional.empty());
        when(autoCommand.getProfile()).thenReturn(Optional.empty());
        when(autoCommand.getResponse()).thenReturn("response");
        when(commandController.getCommandChar()).thenReturn('/');
        when(autoCommandManager.getGlobalAutoCommand()).thenReturn(Optional.of(autoCommand));
        instance = new CoreGlobalAutoCommandsDialogModel(autoCommandManager,
                new ResponseValidator(commandController));
    }

    @Test(expected = IllegalStateException.class)
    public void testLoad_NotCalled() throws Exception {
        instance = new CoreGlobalAutoCommandsDialogModel(autoCommandManager,
                new ResponseValidator(commandController));
        instance.getResponse();
    }

    @Test
    public void testGetResponse_Empty() throws Exception {
        when(autoCommandManager.getGlobalAutoCommand()).thenReturn(Optional.empty());
        instance.load();
        assertEquals("", instance.getResponse());
    }

    @Test
    public void testGetResponse_Complete() throws Exception {
        instance.load();
        assertEquals("response", instance.getResponse());
    }

    @Test(expected = NullPointerException.class)
    public void testSetResponse_Null() throws Exception {
        instance.load();
        assertEquals("response", instance.getResponse());
        instance.setResponse(null);
    }

    @Test
    public void testSetResponse_empty() throws Exception {
        instance.load();
        assertEquals("response", instance.getResponse());
        instance.setResponse("");
        assertEquals("", instance.getResponse());
    }

    @Test
    public void testSetResponse() throws Exception {
        instance.load();
        assertEquals("response", instance.getResponse());
        instance.setResponse("test");
        assertEquals("test", instance.getResponse());
    }

    @Test
    public void testGetResponseValidator() throws Exception {
        instance.load();
        assertNotNull(instance.getResponseValidator());
    }

    @Test
    public void testIsResponseValid_Initially() throws Exception {
        instance.load();
        assertTrue(instance.isResponseValid());
    }

    @Test
    public void testIsResponseValid_CommandChar() throws Exception {
        instance.load();
        instance.setResponse("/moo");
        assertFalse(instance.isResponseValid());
    }

    @Test
    public void testIsResponseValid() throws Exception {
        instance.load();
        instance.setResponse("moo");
        assertTrue(instance.isResponseValid());
    }

    @Test
    public void testIsSaveAllowed() throws Exception {
        instance.load();
        assertTrue(instance.isSaveAllowed());
        instance.setResponse("test");
        assertTrue(instance.isSaveAllowed());
    }

    @Test
    public void testSave_EmptyGlobal() throws Exception {
        when(autoCommandManager.getGlobalAutoCommand()).thenReturn(Optional.empty());
        instance.load();
        instance.setResponse("test");
        assertTrue(instance.isSaveAllowed());
        instance.save();
        verify(autoCommandManager).addAutoCommand(eq(AutoCommand
                .create(Optional.empty(), Optional.empty(), Optional.empty(), "test")));
    }

    @Test
    public void testSave_EmptyResponse() throws Exception {
        instance.load();
        instance.setResponse("");
        assertTrue(instance.isSaveAllowed());
        instance.save();
        verify(autoCommandManager).removeAutoCommand(eq(AutoCommand
                .create(Optional.empty(), Optional.empty(), Optional.empty(), "")));
    }

    @Test
    public void testSave() throws Exception {
        instance.load();
        instance.setResponse("test");
        assertTrue(instance.isSaveAllowed());
        instance.save();
        verify(autoCommandManager).replaceAutoCommand(eq(autoCommand), eq(AutoCommand
                .create(Optional.empty(), Optional.empty(), Optional.empty(), "test")));
    }
}