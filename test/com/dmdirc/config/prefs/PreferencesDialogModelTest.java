/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

import com.dmdirc.events.ClientPrefsClosedEvent;
import com.dmdirc.events.ClientPrefsOpenedEvent;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.Service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import net.engio.mbassy.bus.MBassador;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PreferencesDialogModelTest {

    @Mock private MBassador eventBus;
    @Mock private PluginManager pluginManager;

    @Before
    public void setup() {
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
                null, null, cm, null, pluginManager, eventBus);
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
                null, null, cm, null, pluginManager, eventBus);
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
                null, null, cm, null, pluginManager, eventBus);
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
                null, null, cm, null, pluginManager, eventBus);
        pm.addCategory(category);
        assertTrue(pm.save());

        verify(category).save();
    }

    @Test
    public void testGetCategory() {
        AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");

        final PreferencesDialogModel pm = new PreferencesDialogModel(null, null,
                null, null, cm, null, pluginManager, eventBus);
        assertNull(pm.getCategory("unittest123"));
    }

    @Test
    public void testGetCategories() {
        AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");

        final PreferencesDialogModel pm = new PreferencesDialogModel(null, null,
                null, null, cm, null, pluginManager, eventBus);
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
                null, null, cm, null, pluginManager, eventBus);
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
                null, null, cm, null, pluginManager, eventBus);

        verify(eventBus).publishAsync(isA(ClientPrefsOpenedEvent.class));
    }

    @Test
    public void testCloseAction() {
        final ActionListener tal = mock(ActionListener.class);
        AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");
        final PreferencesDialogModel pm = new PreferencesDialogModel(null, null,
                null, null, cm, null, pluginManager, eventBus);
        pm.close();

        verify(eventBus).publishAsync(isA(ClientPrefsClosedEvent.class));
    }

    @Test
    public void testCategoryObjectSaveListeners() {
        AggregateConfigProvider cm = mock(AggregateConfigProvider.class);
        when(cm.getOption("domain", "option")).thenReturn("fallback");

        final PreferencesDialogModel pm = new PreferencesDialogModel(null, null,
                null, null, cm, null, pluginManager, eventBus);
        final PreferencesCategory category = mock(PreferencesCategory.class);
        final PreferencesInterface tpi = mock(PreferencesInterface.class);
        when(category.hasObject()).thenReturn(true);
        when(category.getObject()).thenReturn(tpi);

        pm.addCategory(category);
        pm.fireSaveListeners();
        verify(tpi).save();
    }

}
