/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

import com.dmdirc.addons.ui_dummy.DummyController;
import com.dmdirc.TestMain;
import com.dmdirc.CustomWindow;
import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_dummy.DummyInputWindow;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.ConfigBinder;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.harness.TestWritableFrameContainer;
import com.dmdirc.interfaces.ui.FrameListener;
import com.dmdirc.interfaces.ui.Window;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class WindowManagerTest {

    private ConfigManager cm;
    private CommandManager commands;

    @Before
    public void setup() throws InvalidIdentityFileException {
        cm = mock(ConfigManager.class);
        final ConfigBinder binder = new ConfigBinder(cm);
        when(cm.getBinder()).thenReturn(binder);
        commands = new CommandManager(cm, TestMain.getTestMain());
    }

    @Test
    public void testAddRoot() {
        final WindowManager manager = new WindowManager();
        final FrameListener tfm = mock(FrameListener.class);
        final Window parent = new DummyInputWindow(new DummyController(TestMain.getTestMain()), new TestWritableFrameContainer(512, cm, commands));

        manager.addListener(tfm);

        verify(tfm, never()).addWindow((FrameContainer) anyObject(), eq(true));
        verify(tfm, never()).addWindow((FrameContainer) anyObject(), (FrameContainer) anyObject(), eq(true));

        manager.addWindow(parent.getContainer());

        verify(tfm).addWindow(same(parent.getContainer()), eq(true));
        verify(tfm, never()).addWindow((FrameContainer) anyObject(), (FrameContainer) anyObject(), eq(true));
    }

    @Test
    public void testAddChild() {
        final WindowManager manager = new WindowManager();
        final FrameListener tfm = mock(FrameListener.class);
        final Window parent = new DummyInputWindow(new DummyController(TestMain.getTestMain()), new TestWritableFrameContainer(512, cm, commands));
        final Window child = new DummyInputWindow(new DummyController(TestMain.getTestMain()), new TestWritableFrameContainer(512, cm, commands));
        manager.addWindow(parent.getContainer());
        manager.addListener(tfm);

        manager.addWindow(parent.getContainer(), child.getContainer());

        verify(tfm, never()).addWindow((FrameContainer) anyObject(), eq(true));
        verify(tfm).addWindow(same(parent.getContainer()), same(child.getContainer()), eq(true));
    }

    @Test
    public void testRemoveRoot() {
        final WindowManager manager = new WindowManager();
        final FrameListener tfm = mock(FrameListener.class);
        final Window parent = new DummyInputWindow(new DummyController(TestMain.getTestMain()), new TestWritableFrameContainer(512, cm, commands));
        manager.addWindow(parent.getContainer());
        manager.addListener(tfm);

        manager.removeWindow(parent.getContainer());

        verify(tfm).delWindow(same(parent.getContainer()));
    }

    @Test
    public void testRemoveChild() {
        final WindowManager manager = new WindowManager();
        final FrameListener tfm = mock(FrameListener.class);
        final Window parent = new DummyInputWindow(new DummyController(TestMain.getTestMain()), new TestWritableFrameContainer(512, cm, commands));
        final Window child = new DummyInputWindow(new DummyController(TestMain.getTestMain()), new TestWritableFrameContainer(512, cm, commands));
        manager.addWindow(parent.getContainer());
        manager.addWindow(parent.getContainer(), child.getContainer());
        manager.addListener(tfm);

        manager.removeWindow(child.getContainer());

        verify(tfm, never()).addWindow((FrameContainer) anyObject(), eq(true));
        verify(tfm, never()).addWindow((FrameContainer) anyObject(), (FrameContainer) anyObject(), eq(true));
        verify(tfm, never()).delWindow((FrameContainer) anyObject());
        verify(tfm).delWindow(same(parent.getContainer()), same(child.getContainer()));
    }

    @Test
    public void testRemoveFrameManager() {
        final WindowManager manager = new WindowManager();
        final FrameListener tfm = mock(FrameListener.class);
        final Window parent = new DummyInputWindow(new DummyController(TestMain.getTestMain()), new TestWritableFrameContainer(512, cm, commands));
        final Window child = new DummyInputWindow(new DummyController(TestMain.getTestMain()), new TestWritableFrameContainer(512, cm, commands));
        manager.addWindow(parent.getContainer());

        manager.addListener(tfm);

        manager.removeListener(tfm);
        manager.addWindow(parent.getContainer(), child.getContainer());

        verify(tfm, never()).addWindow((FrameContainer) anyObject(), eq(true));
        verify(tfm, never()).addWindow((FrameContainer) anyObject(), (FrameContainer) anyObject(), eq(true));
        verify(tfm, never()).delWindow((FrameContainer) anyObject());
        verify(tfm, never()).delWindow((FrameContainer) anyObject(), (FrameContainer) anyObject());
    }

    @Test
    public void testAddListener() {
        final FrameContainer parent = mock(FrameContainer.class);
        final FrameListener fl = mock(FrameListener.class);
        final WindowManager wm = new WindowManager();

        wm.addListener(fl);
        wm.addWindow(parent);

        verify(fl).addWindow(parent, true);
    }

    @Test
    public void testAddListenerAndSync() {
        final FrameContainer parent = mock(FrameContainer.class);
        final FrameContainer child = mock(FrameContainer.class);
        final FrameContainer grandChild = mock(FrameContainer.class);
        final FrameListener fl = mock(FrameListener.class);
        final WindowManager wm = new WindowManager();

        when(parent.getChildren()).thenReturn(Arrays.asList(
                new FrameContainer[]{child, }));
        when(child.getChildren()).thenReturn(Arrays.asList(
                new FrameContainer[]{grandChild, }));

        wm.addWindow(parent);
        wm.addListenerAndSync(fl);

        verify(fl).addWindow(parent, true);
        verify(fl).addWindow(parent, child, true);
        verify(fl).addWindow(child, grandChild, true);
    }

    @Test
    public void testRemoveWindowRootWindowNoChildren() {
        final FrameListener fl = mock(FrameListener.class);
        final FrameContainer parent = mock(FrameContainer.class);
        final WindowManager wm = new WindowManager();

        wm.addListener(fl);
        wm.addWindow(parent);
        wm.removeWindow(parent);

        verify(fl).delWindow(parent);
        verify(parent).windowClosed();
    }

    @Test
    public void testRemoveWindowRootWindowWithChildren() {
        final FrameListener fl = mock(FrameListener.class);
        final FrameContainer parent = mock(FrameContainer.class);
        final FrameContainer child = mock(FrameContainer.class);
        final WindowManager wm = new WindowManager();

        when(parent.getChildren()).thenReturn(Arrays.asList(
                new FrameContainer[]{child, }));
        when(child.getParent()).thenReturn(parent);

        wm.addListener(fl);
        wm.addWindow(parent);
        wm.removeWindow(parent);

        verify(fl).delWindow(parent);
        verify(child).close();
        verify(parent).windowClosed();
    }

    @Test
    public void testRemoveChildWindowNoChildren() {
        final FrameListener fl = mock(FrameListener.class);
        final FrameContainer parent = mock(FrameContainer.class);
        final FrameContainer child = mock(FrameContainer.class);
        final WindowManager wm = new WindowManager();

        when(parent.getChildren()).thenReturn(Arrays.asList(
                new FrameContainer[]{child, }));
        when(child.getParent()).thenReturn(parent);

        wm.addListener(fl);
        wm.addWindow(parent);
        wm.removeWindow(child);

        verify(fl).delWindow(parent, child);
        verify(child).windowClosed();
        verify(parent).removeChild(child);
    }



    @Test
    public void testRemoveChildWindowWithChildren() {
        final FrameListener fl = mock(FrameListener.class);
        final FrameContainer parent = mock(FrameContainer.class);
        final FrameContainer child = mock(FrameContainer.class);
        final FrameContainer grandChild = mock(FrameContainer.class);
        final WindowManager wm = new WindowManager();

        when(parent.getChildren()).thenReturn(Arrays.asList(
                new FrameContainer[]{child, }));
        when(child.getChildren()).thenReturn(Arrays.asList(
                new FrameContainer[]{grandChild, }));
        when(child.getParent()).thenReturn(parent);

        wm.addListener(fl);
        wm.addWindow(parent);
        wm.removeWindow(child);

        verify(fl).delWindow(parent, child);
        verify(grandChild).close();
        verify(child).windowClosed();
        verify(parent).removeChild(child);
    }

    @Test
    public void testGetRootWindows() {
        final FrameContainer root1 = mock(FrameContainer.class);
        final FrameContainer root2 = mock(FrameContainer.class);
        final Collection<FrameContainer> rootWindows
                = Arrays.asList(new FrameContainer[]{root1, root2, });
        final WindowManager wm = new WindowManager();
        wm.addWindow(root1);
        wm.addWindow(root2);

        assertTrue(wm.getRootWindows().containsAll(rootWindows));
    }

    @Test
    public void testFindCustomWindow() {
        final WindowManager wm = new WindowManager();
        final CustomWindow custom = mock(CustomWindow.class);

        when(custom.getName()).thenReturn("test");

        wm.addWindow(custom);

        assertEquals(custom, wm.findCustomWindow("test"));
    }

    @Test
    public void testFindCustomWindowNotFound() {
        final WindowManager wm = new WindowManager();
        final CustomWindow custom = mock(CustomWindow.class);

        when(custom.getName()).thenReturn("test");

        wm.addWindow(custom);

        assertNull(wm.findCustomWindow("test1"));
    }

    @Test
    public void testFindCustomWindowWithParent() {
        final WindowManager wm = new WindowManager();
        final CustomWindow parent = mock(CustomWindow.class);
        final CustomWindow child = mock(CustomWindow.class);

        when(parent.getName()).thenReturn("test");
        when(child.getName()).thenReturn("test1");
        when(parent.getChildren()).thenReturn(Arrays.asList(
                new FrameContainer[]{child, }));

        wm.addWindow(parent);
        wm.addWindow(parent, child);

        assertEquals(child, wm.findCustomWindow(parent, "test1"));
    }

    @Test
    public void testFindCustomWindowWithParentNotFound() {
        final WindowManager wm = new WindowManager();
        final CustomWindow parent = mock(CustomWindow.class);
        final CustomWindow child = mock(CustomWindow.class);

        when(parent.getName()).thenReturn("test");
        when(child.getName()).thenReturn("test1");
        when(parent.getChildren()).thenReturn(Arrays.asList(
                new FrameContainer[]{child, }));

        wm.addWindow(parent);
        wm.addWindow(parent, child);

        assertNull(wm.findCustomWindow(parent, "test"));
    }
}
