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

package com.dmdirc.updater.checking;

import com.dmdirc.updater.UpdateComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NaiveConsolidatorTest {

    private NaiveConsolidator consolidator;

    @Mock private UpdateComponent component1;
    @Mock private UpdateComponent component2;
    @Mock private UpdateComponent component3;
    @Mock private UpdateCheckResult availableResult;
    @Mock private UpdateCheckResult unavailableResult;

    @Before
    public void setUp() {
        when(availableResult.isUpdateAvailable()).thenReturn(true);
        when(unavailableResult.isUpdateAvailable()).thenReturn(false);
        consolidator = new NaiveConsolidator();
    }

    @Test
    public void testIncludesAllComponentsFromAllMaps() {
        final Map<UpdateComponent, UpdateCheckResult> map1 = new HashMap<>();
        map1.put(component1, unavailableResult);
        map1.put(component2, unavailableResult);

        final Map<UpdateComponent, UpdateCheckResult> map2 = new HashMap<>();
        map2.put(component3, unavailableResult);

        final List<Map<UpdateComponent, UpdateCheckResult>> maps = new ArrayList<>();
        maps.add(map1);
        maps.add(map2);

        final Map<UpdateComponent, UpdateCheckResult> res = consolidator.consolidate(maps);
        assertEquals(3, res.size());
        assertTrue(res.containsKey(component1));
        assertTrue(res.containsKey(component2));
        assertTrue(res.containsKey(component3));
    }

    @Test
    public void testIncludesPositiveResultsForKnownNegativeComponents() {
        final Map<UpdateComponent, UpdateCheckResult> map1 = new HashMap<>();
        map1.put(component2, unavailableResult);
        map1.put(component1, unavailableResult);

        final Map<UpdateComponent, UpdateCheckResult> map2 = new HashMap<>();
        map2.put(component2, availableResult);
        map2.put(component3, unavailableResult);

        final List<Map<UpdateComponent, UpdateCheckResult>> maps = new ArrayList<>();
        maps.add(map1);
        maps.add(map2);

        final Map<UpdateComponent, UpdateCheckResult> res = consolidator.consolidate(maps);
        assertTrue(res.containsKey(component2));
        assertSame(availableResult, res.get(component2));
    }

    @Test
    public void testIgnoresNegativeResultsForKnownPositiveComponents() {
        final Map<UpdateComponent, UpdateCheckResult> map1 = new HashMap<>();
        map1.put(component1, unavailableResult);
        map1.put(component2, availableResult);

        final Map<UpdateComponent, UpdateCheckResult> map2 = new HashMap<>();
        map2.put(component2, unavailableResult);
        map2.put(component3, unavailableResult);

        final List<Map<UpdateComponent, UpdateCheckResult>> maps = new ArrayList<>();
        maps.add(map1);
        maps.add(map2);

        final Map<UpdateComponent, UpdateCheckResult> res = consolidator.consolidate(maps);
        assertTrue(res.containsKey(component2));
        assertSame(availableResult, res.get(component2));
    }

}
