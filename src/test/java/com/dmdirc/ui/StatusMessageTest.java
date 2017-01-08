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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.dmdirc.ui;

import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.interfaces.ui.StatusMessageNotifier;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class StatusMessageTest {

    @Test
    public void testGetMessageShortConstructor() {
        final AggregateConfigProvider config = mock(AggregateConfigProvider.class);
        final StatusMessage instance = new StatusMessage("test", config);
        assertEquals("test", instance.getMessage());
    }

    @Test
    public void testGetMessageLongConstructor() {
        final AggregateConfigProvider config = mock(AggregateConfigProvider.class);
        final StatusMessage instance = new StatusMessage("icon", "test", null, 10, config);
        assertEquals("test", instance.getMessage());
    }

    @Test
    public void testGetIconType() {
        final AggregateConfigProvider config = mock(AggregateConfigProvider.class);
        final StatusMessage instance = new StatusMessage("icon", "test", null, 10, config);
        assertEquals("icon", instance.getIconType());
    }

    @Test
    public void testGetMessageNotifierNull() {
        final AggregateConfigProvider config = mock(AggregateConfigProvider.class);
        final StatusMessage instance = new StatusMessage("icon", "test", null, 10, config);
        assertNull(instance.getMessageNotifier());
    }

    @Test
    public void testGetMessageNotifierNotNull() {
        final AggregateConfigProvider config = mock(AggregateConfigProvider.class);
        final StatusMessageNotifier smn = mock(StatusMessageNotifier.class);
        final StatusMessage instance = new StatusMessage("icon", "test", smn, 10, config);
        assertEquals(smn, instance.getMessageNotifier());
    }

    @Test
    public void testGetTimeout() {
        final AggregateConfigProvider config = mock(AggregateConfigProvider.class);
        final StatusMessage instance = new StatusMessage("icon", "test", null, 10, config);
        assertEquals(10, instance.getTimeout());
    }

    @Test
    public void testGetTimeoutFallback() {
        final AggregateConfigProvider config = mock(AggregateConfigProvider.class);

        when(config.getOptionInt("statusBar", "messageDisplayLength")).thenReturn(10);

        final StatusMessage instance = new StatusMessage("icon", "test", null, -1, config);
        assertEquals(10, instance.getTimeout());
    }
}
