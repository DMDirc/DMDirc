/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.harness.TestChangeListener;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

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
                new PreferencesInterface() {
            public void save() {
                // Do nothing
            }
        });

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
    public void testGetSettings() {
        final PreferencesCategory category = new PreferencesCategory("unit", "test");
        final List<PreferencesSetting> settings1 = category.getSettings();
        final List<PreferencesSetting> settings2 = category.getSettings();
        
        assertSame(settings1, settings2);
    }
    
    @Test
    public void testSelectionListener() {
        final PreferencesCategory category = new PreferencesCategory("unit", "test");
        final TestChangeListener test = new TestChangeListener();
        category.addChangeListener(test);
        category.fireCategorySelected();
        category.removeChangeListener(test);
        category.fireCategorySelected();
        
        assertEquals(1, test.selected);
        assertEquals(0, test.deselected);
        assertSame(category, test.cat);
    }
    
    @Test
    public void testDeSelectionListener() {
        final PreferencesCategory category = new PreferencesCategory("unit", "test");
        final TestChangeListener test = new TestChangeListener();
        category.addChangeListener(test);
        category.fireCategoryDeselected();
        category.removeChangeListener(test);
        category.fireCategoryDeselected();
        
        assertEquals(0, test.selected);
        assertEquals(1, test.deselected);
        assertSame(category, test.cat);
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(PreferencesCategoryTest.class);
    }

}
