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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.dmdirc.ui.core.components;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.interfaces.ui.StatusBar;
import com.dmdirc.interfaces.ui.StatusBarComponent;
import com.dmdirc.ui.StatusMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class StatusBarManagerTest {

    @Mock
    private DMDircMBassador eventBus;

    @Test
    public void testRegisterStatusBar() {
        final StatusBar sb = mock(StatusBar.class);
        final StatusBarManager instance = new StatusBarManager(eventBus);
        instance.registerStatusBar(sb);
        instance.clearMessage();
        verify(sb).clearMessage();
    }

    @Test
    public void testUnregisterStatusBar() {
        final StatusBar sb = mock(StatusBar.class);
        final StatusBarManager instance = new StatusBarManager(eventBus);
        instance.registerStatusBar(sb);
        instance.unregisterStatusBar(sb);
        instance.clearMessage();
        verifyZeroInteractions(sb);
    }

    @Test
    public void testSetMessage() {
        final StatusMessage message = mock(StatusMessage.class);
        final StatusBar sb = mock(StatusBar.class);
        final StatusBarManager instance = new StatusBarManager(eventBus);
        instance.registerStatusBar(sb);
        instance.setMessage(message);
        verify(sb).setMessage(message);
    }

    @Test
    public void testClearMessage() {
        final StatusBar sb = mock(StatusBar.class);
        final StatusBarManager instance = new StatusBarManager(eventBus);
        instance.registerStatusBar(sb);
        instance.clearMessage();
        verify(sb).clearMessage();
    }

    @Test
    public void testAddComponent() {
        final StatusBarComponent comp = mock(StatusBarComponent.class);
        final StatusBar sb = mock(StatusBar.class);
        final StatusBarManager instance = new StatusBarManager(eventBus);
        instance.registerStatusBar(sb);
        instance.addComponent(comp);
        verify(sb).addComponent(comp);
    }

    @Test
    public void testRemoveComponent() {
        final StatusBarComponent comp = mock(StatusBarComponent.class);
        final StatusBar sb = mock(StatusBar.class);
        final StatusBarManager instance = new StatusBarManager(eventBus);
        instance.registerStatusBar(sb);
        instance.removeComponent(comp);
        verify(sb).removeComponent(comp);
    }
}
