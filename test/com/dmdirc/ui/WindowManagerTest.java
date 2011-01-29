/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
package com.dmdirc.ui;

import com.dmdirc.FrameContainer;
import com.dmdirc.harness.TestWritableFrameContainer;
import com.dmdirc.addons.ui_dummy.DummyInputWindow;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.ui.interfaces.FrameListener;
import com.dmdirc.ui.interfaces.Window;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class WindowManagerTest {

    @BeforeClass
    public static void setupClass() throws InvalidIdentityFileException {
        IdentityManager.load();
    }

    @Test
    public void testAddRoot() {
        final FrameListener tfm = mock(FrameListener.class);
        final Window parent = new DummyInputWindow(new TestWritableFrameContainer(512, DummyInputWindow.class), null);
        
        WindowManager.addFrameListener(tfm);

        verify(tfm, never()).addWindow((FrameContainer) anyObject(), eq(true));
        verify(tfm, never()).addWindow((FrameContainer) anyObject(), (FrameContainer) anyObject(), eq(true));
        
        WindowManager.addWindow(parent.getContainer());
        
        verify(tfm).addWindow(same(parent.getContainer()), eq(true));
        verify(tfm, never()).addWindow((FrameContainer) anyObject(), (FrameContainer) anyObject(), eq(true));
    }

    @Test
    public void testAddChild() {
        final FrameListener tfm = mock(FrameListener.class);
        final Window parent = new DummyInputWindow(new TestWritableFrameContainer(512, DummyInputWindow.class), null);
        final Window child = new DummyInputWindow(new TestWritableFrameContainer(512, DummyInputWindow.class), null);
        WindowManager.addWindow(parent.getContainer());
        WindowManager.addFrameListener(tfm);
        
        WindowManager.addWindow(parent.getContainer(), child.getContainer());
        
        verify(tfm, never()).addWindow((FrameContainer) anyObject(), eq(true));
        verify(tfm).addWindow(same(parent.getContainer()), same(child.getContainer()), eq(true));
    }

    @Test
    public void testRemoveRoot() {
        final FrameListener tfm = mock(FrameListener.class);
        final Window parent = new DummyInputWindow(new TestWritableFrameContainer(512, DummyInputWindow.class), null);
        WindowManager.addWindow(parent.getContainer());
        WindowManager.addFrameListener(tfm);

        WindowManager.removeWindow(parent.getContainer());
        
        verify(tfm).delWindow(same(parent.getContainer()));
    }

    @Test
    public void testRemoveChild() {
        final FrameListener tfm = mock(FrameListener.class);
        final Window parent = new DummyInputWindow(new TestWritableFrameContainer(512, DummyInputWindow.class), null);
        final Window child = new DummyInputWindow(new TestWritableFrameContainer(512, DummyInputWindow.class), null);
        WindowManager.addWindow(parent.getContainer());
        WindowManager.addWindow(parent.getContainer(), child.getContainer());
        WindowManager.addFrameListener(tfm);

        WindowManager.removeWindow(child.getContainer());

        verify(tfm, never()).addWindow((FrameContainer) anyObject(), eq(true));
        verify(tfm, never()).addWindow((FrameContainer) anyObject(), (FrameContainer) anyObject(), eq(true));
        verify(tfm, never()).delWindow((FrameContainer) anyObject());
        verify(tfm).delWindow(same(parent.getContainer()), same(child.getContainer()));
    }

    @Test
    public void testRemoveFrameManager() {
        final FrameListener tfm = mock(FrameListener.class);
        final Window parent = new DummyInputWindow(new TestWritableFrameContainer(512, DummyInputWindow.class), null);
        final Window child = new DummyInputWindow(new TestWritableFrameContainer(512, DummyInputWindow.class), null);
        WindowManager.addWindow(parent.getContainer());
        
        WindowManager.addFrameListener(tfm);

        WindowManager.removeFrameListener(tfm);
        WindowManager.addWindow(parent.getContainer(), child.getContainer());
        
        verify(tfm, never()).addWindow((FrameContainer) anyObject(), eq(true));
        verify(tfm, never()).addWindow((FrameContainer) anyObject(), (FrameContainer) anyObject(), eq(true));
        verify(tfm, never()).delWindow((FrameContainer) anyObject());
        verify(tfm, never()).delWindow((FrameContainer) anyObject(), (FrameContainer) anyObject());
    }
    
}