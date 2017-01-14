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

package com.dmdirc.updater.manager;

import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.checking.CheckResultConsolidator;

import java.util.concurrent.Executor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CachingUpdateManagerImplTest {

    @Mock private CheckResultConsolidator consolidator;
    @Mock private UpdateComponentPolicy policy;
    @Mock private UpdateComponent checkableComponent;
    @Mock private UpdateComponent uncheckableComponent;
    private CachingUpdateManagerImpl manager;

    @Before
    public void setup() {
        manager = new CachingUpdateManagerImpl(new CurrentThreadExecutor(), consolidator, policy);
        when(policy.canCheck(checkableComponent)).thenReturn(true);
        when(policy.canCheck(uncheckableComponent)).thenReturn(false);
        when(checkableComponent.getName()).thenReturn("checkable");
        when(uncheckableComponent.getName()).thenReturn("uncheckable");
    }

    @Test
    public void testCheckableComponentShownAsIdleWhenAdded() {
        manager.addComponent(checkableComponent);
        assertEquals(UpdateStatus.IDLE, manager.getStatus(checkableComponent));
    }

    @Test
    public void testUncheckableComponentShownAsNotPermittedWhenAdded() {
        manager.addComponent(uncheckableComponent);
        assertEquals(UpdateStatus.CHECKING_NOT_PERMITTED, manager.getStatus(uncheckableComponent));
    }

    private static class CurrentThreadExecutor implements Executor {

        @Override
        public void execute(final Runnable r) {
            r.run();
        }

    }

}
