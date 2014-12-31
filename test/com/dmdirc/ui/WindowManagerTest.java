/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.dmdirc.CustomWindow;
import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.interfaces.ui.FrameListener;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WindowManagerTest {

    @Mock private FrameListener frameListener;
    @Mock private FrameContainer container;
    @Mock private FrameContainer child;
    @Mock private FrameContainer grandchild;
    @Mock private DMDircMBassador eventBus;
    private WindowManager manager;

    @Before
    public void setup() {
        manager = new WindowManager(eventBus);
    }

    @Test
    public void testAddRoot() {
        manager.addListener(frameListener);

        verify(frameListener, never()).addWindow((FrameContainer) anyObject(), anyBoolean());
        verify(frameListener, never()).addWindow((FrameContainer) anyObject(), (FrameContainer) anyObject(), anyBoolean());

        manager.addWindow(container);

        verify(frameListener).addWindow(same(container), eq(true));
        verify(frameListener, never()).addWindow((FrameContainer) anyObject(), (FrameContainer) anyObject(), anyBoolean());
    }

    @Test
    public void testAddChild() {
        manager.addWindow(container);
        manager.addListener(frameListener);
        manager.addWindow(container, child);

        verify(frameListener, never()).addWindow((FrameContainer) anyObject(), anyBoolean());
        verify(frameListener).addWindow(same(container), same(child), eq(true));
    }

    @Test
    public void testRemoveRoot() {
        manager.addWindow(container);
        manager.addListener(frameListener);

        manager.removeWindow(container);

        verify(frameListener).delWindow(same(container));
    }

    @Test
    public void testRemoveChild() {
        manager.addWindow(container);
        manager.addWindow(container, child);
        manager.addListener(frameListener);

        when(child.getParent()).thenReturn(Optional.of(container));

        manager.removeWindow(child);

        verify(frameListener, never()).addWindow((FrameContainer) anyObject(), anyBoolean());
        verify(frameListener, never()).addWindow((FrameContainer) anyObject(), (FrameContainer) anyObject(), anyBoolean());
        verify(frameListener, never()).delWindow((FrameContainer) anyObject());
        verify(frameListener).delWindow(same(container), same(child));
    }

    @Test
    public void testRemoveFrameManager() {
        manager.addWindow(container);
        manager.addListener(frameListener);
        manager.removeListener(frameListener);
        manager.addWindow(container, child);

        verify(frameListener, never()).addWindow((FrameContainer) anyObject(), anyBoolean());
        verify(frameListener, never()).addWindow((FrameContainer) anyObject(), (FrameContainer) anyObject(), anyBoolean());
        verify(frameListener, never()).delWindow((FrameContainer) anyObject());
        verify(frameListener, never()).delWindow((FrameContainer) anyObject(), (FrameContainer) anyObject());
    }

    @Test
    public void testAddListener() {
        manager.addListener(frameListener);
        manager.addWindow(container);
        verify(frameListener).addWindow(container, true);
    }

    @Test
    public void testAddListenerAndSync() {
        when(container.getChildren()).thenReturn(Arrays.asList(child));
        when(child.getChildren()).thenReturn(Arrays.asList(grandchild));

        manager.addWindow(container);
        manager.addListenerAndSync(frameListener);

        verify(frameListener).addWindow(container, true);
        verify(frameListener).addWindow(container, child, true);
        verify(frameListener).addWindow(child, grandchild, true);
    }

    @Test
    public void testRemoveWindowRootWindowNoChildren() {
        manager.addListener(frameListener);
        manager.addWindow(container);
        manager.removeWindow(container);

        verify(frameListener).delWindow(container);
    }

    @Test
    public void testRemoveWindowRootWindowWithChildren() {
        when(container.getChildren()).thenReturn(Arrays.asList(child));
        when(child.getParent()).thenReturn(Optional.of(container));

        manager.addListener(frameListener);
        manager.addWindow(container);
        manager.removeWindow(container);

        verify(frameListener).delWindow(container);
        verify(child).close();
    }

    @Test
    public void testRemoveChildWindowNoChildren() {
        when(container.getChildren()).thenReturn(Arrays.asList(child));
        when(child.getParent()).thenReturn(Optional.of(container));

        manager.addListener(frameListener);
        manager.addWindow(container);
        manager.removeWindow(child);

        verify(frameListener).delWindow(container, child);
        verify(container).removeChild(child);
    }

    @Test
    public void testRemoveChildWindowWithChildren() {
        when(container.getChildren()).thenReturn(Arrays.asList(child));
        when(child.getChildren()).thenReturn(Arrays.asList(grandchild));
        when(child.getParent()).thenReturn(Optional.of(container));

        manager.addListener(frameListener);
        manager.addWindow(container);
        manager.removeWindow(child);

        verify(frameListener).delWindow(container, child);
        verify(grandchild).close();
        verify(container).removeChild(child);
    }

    @Test
    public void testGetRootWindows() {
        final FrameContainer root1 = mock(FrameContainer.class);
        final FrameContainer root2 = mock(FrameContainer.class);
        final Collection<FrameContainer> rootWindows
                = Arrays.asList(root1, root2);

        manager.addWindow(root1);
        manager.addWindow(root2);

        assertTrue(manager.getRootWindows().containsAll(rootWindows));
    }

    @Test
    public void testFindCustomWindow() {
        final CustomWindow custom = mock(CustomWindow.class);

        when(custom.getName()).thenReturn("test");

        manager.addWindow(custom);

        assertEquals(custom, manager.findCustomWindow("test"));
    }

    @Test
    public void testFindCustomWindowNotFound() {
        final CustomWindow custom = mock(CustomWindow.class);

        when(custom.getName()).thenReturn("test");

        manager.addWindow(custom);

        assertNull(manager.findCustomWindow("test1"));
    }

    @Test
    public void testFindCustomWindowWithParent() {
        final CustomWindow customContainer = mock(CustomWindow.class);
        final CustomWindow customChild = mock(CustomWindow.class);

        when(customContainer.getName()).thenReturn("test");
        when(customChild.getName()).thenReturn("test1");
        when(customContainer.getChildren()).thenReturn(Arrays.asList(
                new FrameContainer[]{customChild, }));

        manager.addWindow(customContainer);
        manager.addWindow(customContainer, customChild);

        assertEquals(customChild, manager.findCustomWindow(customContainer, "test1"));
    }

    @Test
    public void testFindCustomWindowWithParentNotFound() {
        final CustomWindow customContainer = mock(CustomWindow.class);
        final CustomWindow customChild = mock(CustomWindow.class);

        when(customContainer.getName()).thenReturn("test");
        when(customChild.getName()).thenReturn("test1");
        when(customContainer.getChildren()).thenReturn(Arrays.asList(
                new FrameContainer[]{customChild, }));

        manager.addWindow(customContainer);
        manager.addWindow(customContainer, customChild);

        assertNull(manager.findCustomWindow(customContainer, "test"));
    }
}
