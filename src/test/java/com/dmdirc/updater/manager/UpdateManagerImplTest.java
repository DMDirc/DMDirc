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
import com.dmdirc.updater.checking.UpdateCheckResult;
import com.dmdirc.updater.retrieving.UpdateRetrievalStrategy;

import java.util.concurrent.Executor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateManagerImplTest {

    @Mock private Executor executor;
    @Mock private CheckResultConsolidator checkResultConsolidator;
    @Mock private UpdateComponentPolicy updateComponentPolicy;
    @Mock private UpdateComponent component1;
    @Mock private UpdateComponent component2;
    @Mock private UpdateComponent component3;
    @Mock private UpdateStatusListener statusListener;
    @Mock private UpdateCheckResult checkResult1;
    @Mock private UpdateCheckResult checkResult2;
    @Mock private UpdateRetrievalStrategy strategy1;
    @Mock private UpdateRetrievalStrategy strategy2;
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

        doAnswer(invocation -> {
            // Add a new component from the manager while it's checking for updates.
            // This potentially causes a CME, see CLIENT-404.
            manager.addComponent(component3);
            return null;
        }).when(statusListener).updateStatusChanged(component1, UpdateStatus.CHECKING, 0);

        when(updateComponentPolicy.canCheck(any(UpdateComponent.class))).thenReturn(true);

        manager.checkForUpdates();
    }

    @Test
    public void testGetSingleRetrievalStrategy() {
        // Given a single retrieval strategy that can handle anything
        when(strategy1.canHandle(any())).thenReturn(true);
        manager.addRetrievalStrategy(strategy1);

        // Then it is returned for any result.
        assertSame(strategy1, manager.getStrategy(checkResult1));
        assertSame(strategy1, manager.getStrategy(checkResult2));
    }

    @Test
    public void testGetMultipleRetrievalStrategy() {
        // Given two retrieval strategies that can handle different check results
        when(strategy1.canHandle(checkResult1)).thenReturn(true);
        when(strategy2.canHandle(checkResult2)).thenReturn(true);
        manager.addRetrievalStrategy(strategy1);
        manager.addRetrievalStrategy(strategy2);

        // Then the corresponding strategy is returned for each result
        assertSame(strategy1, manager.getStrategy(checkResult1));
        assertSame(strategy2, manager.getStrategy(checkResult2));
    }

    @Test
    public void testGetNoRetrievalStrategy() {
        // Given a retrieval strategies that can handle a specific check result
        manager.addRetrievalStrategy(strategy1);

        // Then null is returned for other check results
        assertNull(manager.getStrategy(checkResult2));
    }

}
