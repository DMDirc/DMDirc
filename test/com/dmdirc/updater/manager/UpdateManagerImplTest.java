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

package com.dmdirc.updater.manager;

import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.checking.CheckResultConsolidator;

import java.util.concurrent.Executor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdateManagerImplTest {

    @Mock private Executor executor;
    @Mock private CheckResultConsolidator checkResultConsolidator;
    @Mock private UpdateComponentPolicy updateComponentPolicy;
    @Mock private UpdateComponent component1;
    @Mock private UpdateComponent component2;
    @Mock private UpdateComponent component3;
    @Mock private UpdateStatusListener statusListener;
    private UpdateManagerImpl manager;

    @Before
    public void setup() {
        manager = new UpdateManagerImpl(executor, checkResultConsolidator, updateComponentPolicy);
        when(component1.getName()).thenReturn("test1");
        when(component2.getName()).thenReturn("test2");
        when(component3.getName()).thenReturn("test3");
    }

    @Test
    public void testRemovingComponentInStatusChangeListener() {
        manager.addComponent(component1);
        manager.addComponent(component2);

        manager.addUpdateStatusListener(statusListener);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                // Add a new component from the manager while it's checking for updates.
                // This potentially causes a CME, see CLIENT-404.
                manager.addComponent(component3);
                return null;
            }
        }).when(statusListener).updateStatusChanged(component1, UpdateStatus.CHECKING, 0);

        when(updateComponentPolicy.canCheck(any(UpdateComponent.class))).thenReturn(true);

        manager.checkForUpdates();
    }

}
