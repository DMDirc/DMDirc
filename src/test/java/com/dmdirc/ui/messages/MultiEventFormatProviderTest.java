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

package com.dmdirc.ui.messages;

import com.dmdirc.events.ChannelModesDiscoveredEvent;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MultiEventFormatProviderTest {

    @Mock private EventFormat mockEventFormat1;
    @Mock private EventFormat mockEventFormat2;
    @Mock private EventFormatProvider mockEventFormatProvider1;
    @Mock private EventFormatProvider mockEventFormatProvider2;

    @Before
    public void setup() {
        when(mockEventFormatProvider1.getFormat(ChannelModesDiscoveredEvent.class))
                .thenReturn(Optional.of(mockEventFormat1));
        when(mockEventFormatProvider2.getFormat(ChannelModesDiscoveredEvent.class))
                .thenReturn(Optional.of(mockEventFormat2));
    }

    @Test
    public void testReturnsEmptyWithNoProviders() {
        final EventFormatProvider provider = new MultiEventFormatProvider();
        assertFalse(provider.getFormat(ChannelModesDiscoveredEvent.class).isPresent());
    }

    @Test
    public void testReturnsFormatFromProvider() {
        final EventFormatProvider provider = new MultiEventFormatProvider(mockEventFormatProvider1);
        final Optional<EventFormat> res = provider.getFormat(ChannelModesDiscoveredEvent.class);
        assertTrue(res.isPresent());
        assertSame(mockEventFormat1, res.get());
    }

    @Test
    public void testReturnsFormatFromAlternateProvider() {
        final MultiEventFormatProvider provider =
                new MultiEventFormatProvider(mockEventFormatProvider1);
        provider.addProvider(mockEventFormatProvider2);

        final Optional<EventFormat> res = provider.getFormat(ChannelModesDiscoveredEvent.class);
        assertTrue(res.isPresent());
        assertSame(mockEventFormat1, res.get());
    }

    @Test
    public void testReturnsFirstFormatIfMultipleProvidersHaveFormats() {
        final MultiEventFormatProvider provider =
                new MultiEventFormatProvider(mockEventFormatProvider1);
        provider.addProvider(mockEventFormatProvider2);

        final Optional<EventFormat> res = provider.getFormat(ChannelModesDiscoveredEvent.class);
        assertTrue(res.isPresent());
        assertSame(mockEventFormat1, res.get());
    }

    @Test
    public void testRemovesProvider() {
        final MultiEventFormatProvider provider =
                new MultiEventFormatProvider(mockEventFormatProvider1);
        provider.addProvider(mockEventFormatProvider2);
        provider.removeProvider(mockEventFormatProvider1);

        final Optional<EventFormat> res = provider.getFormat(ChannelModesDiscoveredEvent.class);
        assertTrue(res.isPresent());
        assertSame(mockEventFormat2, res.get());

        provider.removeProvider(mockEventFormatProvider2);
        assertFalse(provider.getFormat(ChannelModesDiscoveredEvent.class).isPresent());
    }

}