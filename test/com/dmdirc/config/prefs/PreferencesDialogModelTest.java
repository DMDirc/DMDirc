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

package com.dmdirc.config.prefs;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.Service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PreferencesDialogModelTest {

    private ActionManager actionManager;
    private PluginManager pluginManager;

    @Before
    public void setup() {
        actionManager = mock(ActionManager.class);
        pluginManager = mock(PluginManager.class);

        final List<Service> services = new ArrayList<>();
        final Service tabcompleter = mock(Service.class);
        when(tabcompleter.getName()).thenReturn("tabber");
        services.add(tabcompleter);

        when(pluginManager.getServicesByType("tabcompletion")).thenReturn(services);
    }

    @Test
    public void testDefaults() {
        AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");
        final PreferencesDialogModel pm = new PreferencesDialogModel(null, null,
                null, null, cm, null, actionManager, pluginManager);
        assertNotNull(pm.getCategory("General"));
        assertNotNull(pm.getCategory("Connection"));
        assertNotNull(pm.getCategory("Messages"));
        assertNotNull(pm.getCategory("Advanced"));
        assertNotNull(pm.getCategory("GUI"));
        assertNotNull(pm.getCategory("Plugins"));
        assertNotNull(pm.getCategory("Updates"));
        assertNotNull(pm.getCategory("URL Handlers"));
    }

    @Test
    public void testDismiss() {
        AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");
        final PreferencesCategory category = mock(PreferencesCategory.class);
        final PreferencesDialogModel pm = new PreferencesDialogModel(null, null,
                null, null, cm, null, actionManager, pluginManager);
        pm.addCategory(category);
        pm.dismiss();

        verify(category).dismiss();
    }

    @Test
    public void testSaveNoRestart() {
        final PreferencesCategory category = mock(PreferencesCategory.class);
        when(category.save()).thenReturn(false);
        AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");

        final PreferencesDialogModel pm = new PreferencesDialogModel(null, null,
                null, null, cm, null, actionManager, pluginManager);
        pm.addCategory(category);
        assertFalse(pm.save());

        verify(category).save();
    }

    @Test
    public void testSaveRestart() {
        final PreferencesCategory category = mock(PreferencesCategory.class);
        when(category.save()).thenReturn(true);
        AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");

        final PreferencesDialogModel pm = new PreferencesDialogModel(null, null,
                null, null, cm, null, actionManager, pluginManager);
        pm.addCategory(category);
        assertTrue(pm.save());

        verify(category).save();
    }

    @Test
    public void testGetCategory() {
        AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");

        final PreferencesDialogModel pm = new PreferencesDialogModel(null, null,
                null, null, cm, null, actionManager, pluginManager);
        assertNull(pm.getCategory("unittest123"));
    }

    @Test
    public void testGetCategories() {
        AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");

        final PreferencesDialogModel pm = new PreferencesDialogModel(null, null,
                null, null, cm, null, actionManager, pluginManager);
        assertNotNull(pm.getCategories());
        assertFalse(pm.getCategories().isEmpty());

        for (PreferencesCategory cat : pm.getCategories()) {
            assertNotNull(pm.getCategory(cat.getTitle()));
        }
    }

    @Test
    public void testSaveListener() {
        AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");

        final PreferencesDialogModel pm = new PreferencesDialogModel(null, null,
                null, null, cm, null, actionManager, pluginManager);
        final PreferencesInterface tpi = mock(PreferencesInterface.class);

        pm.registerSaveListener(tpi);
        pm.fireSaveListeners();
        verify(tpi).save();
    }

    @Test
    public void testOpenAction() {
        AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");

        final PreferencesDialogModel pm = new PreferencesDialogModel(null, null,
                null, null, cm, null, actionManager, pluginManager);

        verify(actionManager).triggerEvent(CoreActionType.CLIENT_PREFS_OPENED, null, pm);
    }

    @Test
    public void testCloseAction() {
        final ActionListener tal = mock(ActionListener.class);
        AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");
        final PreferencesDialogModel pm = new PreferencesDialogModel(null, null,
                null, null, cm, null, actionManager, pluginManager);
        pm.close();

        verify(actionManager).triggerEvent(CoreActionType.CLIENT_PREFS_CLOSED, null);
    }

    @Test
    public void testCategoryObjectSaveListeners() {
        AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");

        final PreferencesDialogModel pm = new PreferencesDialogModel(null, null,
                null, null, cm, null, actionManager, pluginManager);
        final PreferencesCategory category = mock(PreferencesCategory.class);
        final PreferencesInterface tpi = mock(PreferencesInterface.class);
        when(category.hasObject()).thenReturn(true);
        when(category.getObject()).thenReturn(tpi);

        pm.addCategory(category);
        pm.fireSaveListeners();
        verify(tpi).save();
    }

}
