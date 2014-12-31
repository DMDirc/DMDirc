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

package com.dmdirc.config.prefs;

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PreferencesCategoryTest {

    @Test
    public void testInline() {
        final PreferencesCategory category = new PreferencesCategory("unit", "test");
        assertFalse(category.isInline());
        assertSame(category, category.setInline());
        assertTrue(category.isInline());
    }

    @Test
    public void testInlineBefore() {
        final PreferencesCategory category = new PreferencesCategory("unit", "test");
        assertTrue(category.isInlineBefore());
        assertSame(category, category.setInlineAfter());
        assertFalse(category.isInlineBefore());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddWithObject() {
        final PreferencesCategory category = new PreferencesCategory("unit", "test",
                mock(PreferencesInterface.class));

        category.addSetting(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInlineAdding() {
        final PreferencesCategory category1 = new PreferencesCategory("unit", "test");
        final PreferencesCategory category2 = new PreferencesCategory("unit", "test");

        category1.setInline();
        category1.addSubCategory(category2);
    }

    @Test
    public void testDescription() {
        final PreferencesCategory category = new PreferencesCategory("unit", "This is a desc.");

        assertEquals("This is a desc.", category.getDescription());
    }

    @Test
    public void testIcon() {
        final PreferencesCategory category = new PreferencesCategory("unit",
                "This is a desc.", "icon");

        assertEquals("icon", category.getIcon());
    }

    @Test
    public void testDefaultWarning() {
        final PreferencesCategory category = new PreferencesCategory("unit",
                "This is a desc.", "icon");

        assertNull(category.getWarning());
    }

    @Test
    public void testSetWarning() {
        final PreferencesCategory category = new PreferencesCategory("unit",
                "This is a desc.", "icon");
        category.setWarning("warning 123");
        assertEquals("warning 123", category.getWarning());
    }

    @Test
    public void testParent() {
        final PreferencesCategory parent = mock(PreferencesCategory.class);
        when(parent.getPath()).thenReturn("parent path");

        final PreferencesCategory category = new PreferencesCategory("unit",
                "This is a desc.");
        category.setParent(parent);

        assertSame(parent, category.getParent());
        assertEquals("parent path → unit", category.getPath());
    }

    @Test
    public void testGetSettings() {
        final PreferencesCategory category = new PreferencesCategory("unit", "test");
        final List<PreferencesSetting> settings1 = category.getSettings();
        final List<PreferencesSetting> settings2 = category.getSettings();

        assertSame(settings1, settings2);
    }

    @Test
    public void testSelectionListener() {
        final PreferencesCategory category = new PreferencesCategory("unit", "test");
        final CategoryChangeListener test = mock(CategoryChangeListener.class);

        category.addChangeListener(test);
        category.fireCategorySelected();
        category.removeChangeListener(test);
        category.fireCategorySelected();

        verify(test).categorySelected(category);
        verify(test, never()).categoryDeselected((PreferencesCategory) anyObject());
    }

    @Test
    public void testDeSelectionListener() {
        final PreferencesCategory category = new PreferencesCategory("unit", "test");
        final CategoryChangeListener test = mock(CategoryChangeListener.class);

        category.addChangeListener(test);
        category.fireCategoryDeselected();
        category.removeChangeListener(test);
        category.fireCategoryDeselected();

        verify(test, never()).categorySelected(category);
        verify(test).categoryDeselected((PreferencesCategory) anyObject());
    }

    @Test
    public void testDismiss() {
        final PreferencesCategory category = new PreferencesCategory("unit", "test");
        final PreferencesSetting setting = mock(PreferencesSetting.class);
        category.addSetting(setting);
        category.dismiss();

        verify(setting).dismiss();
    }

    @Test
    public void testSaveNotUpdated() {
        final PreferencesCategory category = new PreferencesCategory("unit", "test");
        final PreferencesSetting setting = mock(PreferencesSetting.class);
        when(setting.save()).thenReturn(false);

        category.addSetting(setting);
        assertFalse(category.save());

        verify(setting).save();
    }

    @Test
    public void testSaveUpdatedNoRestart() {
        final PreferencesCategory category = new PreferencesCategory("unit", "test");
        final PreferencesSetting setting = mock(PreferencesSetting.class);
        when(setting.save()).thenReturn(true);
        when(setting.isRestartNeeded()).thenReturn(false);

        category.addSetting(setting);
        assertFalse(category.save());

        verify(setting).save();
    }

    @Test
    public void testSaveUpdatedRestart() {
        final PreferencesCategory category = new PreferencesCategory("unit", "test");
        final PreferencesSetting setting = mock(PreferencesSetting.class);
        when(setting.save()).thenReturn(true);
        when(setting.isRestartNeeded()).thenReturn(true);

        category.addSetting(setting);
        assertTrue(category.save());

        verify(setting).save();
    }

    @Test
    public void testGetPathNoParent() {
        final PreferencesCategory category = new PreferencesCategory("unit", "test");
        assertEquals("unit", category.getPath());
    }

    @Test
    public void testGetPathWithParent() {
        final PreferencesCategory parent = new PreferencesCategory("unit1", "test1");
        final PreferencesCategory child = new PreferencesCategory("unit2", "test2");
        child.setParent(parent);
        assertEquals("unit1 → unit2", child.getPath());
    }
}
