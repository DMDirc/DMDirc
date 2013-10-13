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

package com.dmdirc.updater.checking;

import com.dmdirc.updater.UpdateComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NaiveConsolidatorTest {

    private NaiveConsolidator consolidator;
    private List<UpdateComponent> components;
    private List<UpdateCheckResult> positiveResults;
    private List<UpdateCheckResult> negativeResults;

    @Before
    public void setUp() {
        consolidator = new NaiveConsolidator();
        components = new ArrayList<>();
        positiveResults = new ArrayList<>();
        negativeResults = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            final UpdateComponent component = mock(UpdateComponent.class);
            final UpdateCheckResult negative = mock(UpdateCheckResult.class);
            final UpdateCheckResult positive = mock(UpdateCheckResult.class);
            when(negative.getComponent()).thenReturn(component);
            when(negative.isUpdateAvailable()).thenReturn(false);
            when(positive.getComponent()).thenReturn(component);
            when(positive.isUpdateAvailable()).thenReturn(true);

            components.add(component);
            negativeResults.add(negative);
            positiveResults.add(negative);
        }
    }

    @Test
    public void testIncludesAllComponentsFromAllMaps() {
        final Map<UpdateComponent, UpdateCheckResult> map1
                = new HashMap<>();
        final Map<UpdateComponent, UpdateCheckResult> map2
                = new HashMap<>();
        final List<Map<UpdateComponent, UpdateCheckResult>> maps
                = new ArrayList<>();

        map1.put(components.get(0), negativeResults.get(0));
        map1.put(components.get(1), negativeResults.get(1));
        map2.put(components.get(2), negativeResults.get(2));

        maps.add(map1);
        maps.add(map2);

        final Map<UpdateComponent, UpdateCheckResult> res = consolidator.consolidate(maps);
        assertEquals(3, res.size());
        assertTrue(res.containsKey(components.get(0)));
        assertTrue(res.containsKey(components.get(1)));
        assertTrue(res.containsKey(components.get(2)));
    }

    @Test
    public void testIncludesPositiveResultsForKnownNegativeComponents() {
        final Map<UpdateComponent, UpdateCheckResult> map1
                = new HashMap<>();
        final Map<UpdateComponent, UpdateCheckResult> map2
                = new HashMap<>();
        final List<Map<UpdateComponent, UpdateCheckResult>> maps
                = new ArrayList<>();

        map1.put(components.get(0), negativeResults.get(0));
        map1.put(components.get(1), negativeResults.get(1));
        map2.put(components.get(1), positiveResults.get(1));
        map2.put(components.get(2), negativeResults.get(2));

        maps.add(map1);
        maps.add(map2);

        final Map<UpdateComponent, UpdateCheckResult> res = consolidator.consolidate(maps);
        assertTrue(res.containsKey(components.get(1)));
        assertSame(positiveResults.get(1), res.get(components.get(1)));
    }

    @Test
    public void testIgnoresNegativeResultsForKnownPositiveComponents() {
        final Map<UpdateComponent, UpdateCheckResult> map1
                = new HashMap<>();
        final Map<UpdateComponent, UpdateCheckResult> map2
                = new HashMap<>();
        final List<Map<UpdateComponent, UpdateCheckResult>> maps
                = new ArrayList<>();

        map1.put(components.get(0), negativeResults.get(0));
        map1.put(components.get(1), positiveResults.get(1));
        map2.put(components.get(1), negativeResults.get(1));
        map2.put(components.get(2), negativeResults.get(2));

        maps.add(map1);
        maps.add(map2);

        final Map<UpdateComponent, UpdateCheckResult> res = consolidator.consolidate(maps);
        assertTrue(res.containsKey(components.get(1)));
        assertSame(positiveResults.get(1), res.get(components.get(1)));
    }
}
