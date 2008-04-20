/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.harness.TestFrameManager;
import com.dmdirc.ui.dummy.DummyInputWindow;
import com.dmdirc.ui.interfaces.Window;

import org.junit.Test;
import static org.junit.Assert.*;

public class WindowManagerTest extends junit.framework.TestCase {

    @Test
    public void testFrameManagers() {
        final TestFrameManager tfm = new TestFrameManager();
        final Window parent = new DummyInputWindow(null, null);
        final Window child = new DummyInputWindow(null, null);
        
        WindowManager.addFrameManager(tfm);
        
        assertEquals(0, tfm.orphans);
        assertEquals(0, tfm.children);
        
        WindowManager.addWindow(parent);
        
        assertEquals(1, tfm.orphans);
        assertEquals(0, tfm.children);
        
        WindowManager.addWindow(parent, child);
        
        assertEquals(1, tfm.orphans);
        assertEquals(1, tfm.children);
        
        WindowManager.removeWindow(parent);
        
        assertEquals(0, tfm.orphans);
        assertEquals(0, tfm.children);  
        
        WindowManager.removeWindow(child);
        
        assertEquals(0, tfm.orphans);
        assertEquals(0, tfm.children);       
        
        WindowManager.removeFrameManager(tfm);
        WindowManager.addWindow(parent);
        
        assertEquals(0, tfm.orphans);
        assertEquals(0, tfm.children);       
    }
    
    @Test
    public void testGetParent() {
        final Window parent1 = new DummyInputWindow(null, null);
        final Window parent2 = new DummyInputWindow(null, null);
        final Window child1 = new DummyInputWindow(null, null);
        final Window child2 = new DummyInputWindow(null, null);
        
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