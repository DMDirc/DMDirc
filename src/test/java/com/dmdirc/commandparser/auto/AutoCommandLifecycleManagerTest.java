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

package com.dmdirc.commandparser.auto;

import com.google.common.collect.Sets;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AutoCommandLifecycleManagerTest {

    @Mock private AutoCommandManager autoCommandManager;
    @Mock private AutoCommandStore autoCommandStore;
    @Mock private AutoCommand command1;
    @Mock private AutoCommand command2;
    @Mock private AutoCommand command3;
    private AutoCommandLifecycleManager autoCommandLifecycleManager;
    private Set<AutoCommand> getCommands;

    @Before
    public void setup() {
        autoCommandLifecycleManager = new AutoCommandLifecycleManager(autoCommandManager,
                autoCommandStore);
        getCommands = Sets.newHashSet(command1, command2, command3);
        when(autoCommandStore.readAutoCommands()).thenReturn(
                Sets.newHashSet(command1, command2));
        when(autoCommandManager.getAutoCommands()).thenReturn(getCommands);
    }

    @Test
    public void testStartUp() {
        autoCommandLifecycleManager.startUp();
        verify(autoCommandStore).readAutoCommands();
        verify(autoCommandManager).addAutoCommand(command1);
        verify(autoCommandManager).addAutoCommand(command2);
        verify(autoCommandManager).start();
    }

    @Test
    public void testShutDown() {
        autoCommandLifecycleManager.shutDown();
        verify(autoCommandManager).stop();
        verify(autoCommandStore).writeAutoCommands(getCommands);
    }
}