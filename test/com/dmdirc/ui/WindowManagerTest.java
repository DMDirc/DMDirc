/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.ui.interfaces.FrameManager;
import com.dmdirc.ui.interfaces.Window;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class WindowManagerTest {

    @Test
    public void testAddRoot() {
        final FrameManager tfm = mock(FrameManager.class);
        final Window parent = new DummyInputWindow(new TestWritableFrameContainer(512), null);
        
        WindowManager.addFrameManager(tfm);

        verify(tfm, never()).addWindow((FrameContainer) anyObject());
        verify(tfm, never()).addWindow((FrameContainer) anyObject(), (FrameContainer) anyObject());
        
        WindowManager.addWindow(parent);
        
        verify(tfm).addWindow(same(parent.getContainer()));
        verify(tfm, never()).addWindow((FrameContainer) anyObject(), (FrameContainer) anyObject());
    }

    @Test
    public void testAddChild() {
        final FrameManager tfm = mock(FrameManager.class);
        final Window parent = new DummyInputWindow(new TestWritableFrameContainer(512), null);
        final Window child = new DummyInputWindow(new TestWritableFrameContainer(512), null);
        WindowManager.addWindow(parent);
        WindowManager.addFrameManager(tfm);
        
        WindowManager.addWindow(parent, child);
        
        verify(tfm, never()).addWindow((FrameContainer) anyObject());
        verify(tfm).addWindow(same(parent.getContainer()), same(child.getContainer()));
    }

    @Test
    public void testRemoveRoot() {
        final FrameManager tfm = mock(FrameManager.class);
        final Window parent = new DummyInputWindow(new TestWritableFrameContainer(512), null);
        final Window child = new DummyInputWindow(new TestWritableFrameContainer(512), null);
        WindowManager.addWindow(parent);
        WindowManager.addFrameManager(tfm);

        WindowManager.removeWindow(parent);
        
        verify(tfm).delWindow(same(parent.getContainer()));
    }

    @Test
    public void testRemoveChild() {
        final FrameManager tfm = mock(FrameManager.class);
        final Window parent = new DummyInputWindow(new TestWritableFrameContainer(512), null);
        final Window child = new DummyInputWindow(new TestWritableFrameContainer(512), null);
        WindowManager.addWindow(parent);
        WindowManager.addWindow(parent, child);
        WindowManager.addFrameManager(tfm);

        WindowManager.removeWindow(child);

        verify(tfm, never()).addWindow((FrameContainer) anyObject());
        verify(tfm, never()).addWindow((FrameContainer) anyObject(), (FrameContainer) anyObject());
        verify(tfm, never()).delWindow((FrameContainer) anyObject());
        verify(tfm).delWindow(same(parent.getContainer()), same(child.getContainer()));
    }

    @Test
    public void testRemoveFrameManager() {
        final FrameManager tfm = mock(FrameManager.class);
        final Window parent = new DummyInputWindow(new TestWritableFrameContainer(512), null);
        final Window child = new DummyInputWindow(new TestWritableFrameContainer(512), null);
        WindowManager.addWindow(parent);
        
        WindowManager.addFrameManager(tfm);

        WindowManager.removeFrameManager(tfm);
        WindowManager.addWindow(parent, child);
        
        verify(tfm, never()).addWindow((FrameContainer) anyObject());
        verify(tfm, never()).addWindow((FrameContainer) anyObject(), (FrameContainer) anyObject());
        verify(tfm, never()).delWindow((FrameContainer) anyObject());
        verify(tfm, never()).delWindow((FrameContainer) anyObject(), (FrameContainer) anyObject());
    }
    
    @Test
    public void testGetParent() {
        final Window parent1 = new DummyInputWindow(new TestWritableFrameContainer(512), null);
        final Window parent2 = new DummyInputWindow(new TestWritableFrameContainer(512), null);
        final Window child1 = new DummyInputWindow(new TestWritableFrameContainer(512), null);
        final Window child2 = new DummyInputWindow(new TestWritableFrameContainer(512), null);
        
        WindowManager.addWindow(parent1);
        WindowManager.addWindow(parent2);
        WindowManager.addWindow(parent1, child1);
        WindowManager.addWindow(parent2, child2);
        
        assertSame(parent1, WindowManager.getParent(child1));
        assertSame(parent2, WindowManager.getParent(child2));
        assertNull(WindowManager.getParent(parent1));
        assertNull(WindowManager.getParent(parent2));
    }
    
}