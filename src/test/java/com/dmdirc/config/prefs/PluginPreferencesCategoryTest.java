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
package com.dmdirc.config.prefs;

import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginMetaData;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PluginPreferencesCategoryTest {

    private static PluginInfo loaded, unloaded;
    private static PluginMetaData metaData;
    private static PreferencesInterface obj;

    @BeforeClass
    public static void setupClass() {
        metaData = mock(PluginMetaData.class);
        when(metaData.getFriendlyName()).thenReturn("nice name");

        loaded = mock(PluginInfo.class);
        when(loaded.isLoaded()).thenReturn(true);
        when(loaded.getMetaData()).thenReturn(metaData);

        unloaded = mock(PluginInfo.class);
        when(unloaded.isLoaded()).thenReturn(false);
        when(unloaded.getMetaData()).thenReturn(metaData);

        obj = mock(PreferencesInterface.class);
    }

    @Test
    public void testCtor1() {
        final PreferencesCategory category = new PluginPreferencesCategory(loaded,
                "unit", "This is a desc.", "icon");

        assertEquals("icon", category.getIcon());
        assertEquals("This is a desc.", category.getDescription());
        assertEquals("unit", category.getTitle());
    }

    @Test
    public void testCtor2() {
        final PreferencesCategory category = new PluginPreferencesCategory(loaded,
                "unit", "This is a desc.");

        assertEquals("This is a desc.", category.getDescription());
        assertEquals("unit", category.getTitle());
    }

    @Test
    public void testCtor3() {
        final PreferencesCategory category = new PluginPreferencesCategory(loaded,
                "unit", "This is a desc.", "icon", obj);

        assertEquals("icon", category.getIcon());
        assertEquals("This is a desc.", category.getDescription());
        assertEquals("unit", category.getTitle());
        assertSame(obj, category.getObject());
    }

    @Test
    public void testCtor4() {
        final PreferencesCategory category = new PluginPreferencesCategory(loaded,
                "unit", "This is a desc.", obj);

        assertEquals("This is a desc.", category.getDescription());
        assertEquals("unit", category.getTitle());
        assertSame(obj, category.getObject());
    }

    @Test
    public void testLoadedWarning() {
        final PreferencesCategory category = new PluginPreferencesCategory(loaded, "unit",
                "This is a desc.");

        assertNull(category.getWarning());
    }

    @Test
    public void testUnloadedWarning() {
        final PreferencesCategory category = new PluginPreferencesCategory(unloaded, "unit",
                "This is a desc.");

        assertTrue(category.getWarning().contains("'nice name'"));
        assertTrue(category.getWarning().contains("not currently loaded"));
    }

}
