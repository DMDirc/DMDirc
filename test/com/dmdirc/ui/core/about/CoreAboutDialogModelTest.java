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

package com.dmdirc.ui.core.about;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.PluginMetaData;
import com.dmdirc.util.ClientInfo;
import com.dmdirc.util.io.FileUtils;

import com.google.common.collect.Lists;

import java.nio.file.Path;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CoreAboutDialogModelTest {

    @Mock private ClientInfo clientInfo;
    @Mock private AggregateConfigProvider globalConfig;
    @Mock private Path path;
    @Mock private DMDircMBassador eventBus;
    @Mock private PluginManager pluginManager;
    @Mock private PluginMetaData pluginMetaData1;
    @Mock private PluginMetaData pluginMetaData2;
    @Mock private PluginInfo pluginInfo1;
    @Mock private PluginInfo pluginInfo2;
    private Path pluginPath1;
    private Path pluginPath2;
    private CoreAboutDialogModel instance;

    @Before
    public void setUp() throws Exception {
        pluginPath1 = FileUtils.getPathForResource(getClass().getResource("license1"));
        pluginPath2 = FileUtils.getPathForResource(getClass().getResource("license2"));
        when(clientInfo.getVersionInformation()).thenReturn("DMDirc Version");
        when(clientInfo.getOperatingSystemInformation()).thenReturn("OS Version");
        when(clientInfo.getJavaInformation()).thenReturn("Java Version");
        when(globalConfig.getOption("identity", "modealiasversion")).thenReturn("ModeAlias Version");
        when(pluginManager.getPluginInfos()).thenReturn(Lists.newArrayList(pluginInfo1, pluginInfo2));
        when(pluginInfo1.getPath("/META-INF/licenses/")).thenReturn(pluginPath1);
        when(pluginInfo2.getPath("/META-INF/licenses/")).thenReturn(pluginPath2);
        when(pluginInfo1.getMetaData()).thenReturn(pluginMetaData1);
        when(pluginInfo2.getMetaData()).thenReturn(pluginMetaData2);
        when(pluginMetaData1.getFriendlyName()).thenReturn("Plugin1");
        when(pluginMetaData2.getFriendlyName()).thenReturn("Plugin2");
        instance = new CoreAboutDialogModel(globalConfig, path, clientInfo, eventBus, pluginManager);
        instance.load();
    }

    @Test
    public void testGetAbout() throws Exception {
        final String about = instance.getAbout();
        assertTrue(about.contains("DMDirc"));
        assertTrue(about.contains("The intelligent IRC client."));
        assertTrue(about.contains("<a href=\"https://www.dmdirc.com\">www.dmdirc.com</a>"));
    }

    @Test
    public void testGetMainDevelopers() throws Exception {
        assertEquals(3, instance.getMainDevelopers().size());
        assertTrue(instance.getMainDevelopers().get(0).getName().contains("MD87"));
        assertTrue(instance.getMainDevelopers().get(1).getName().contains("Greboid"));
        assertTrue(instance.getMainDevelopers().get(2).getName().contains("Dataforce"));
    }

    @Test
    public void testGetOtherDevelopers() throws Exception {
        assertEquals(1, instance.getOtherDevelopers().size());
        assertTrue(instance.getOtherDevelopers().get(0).getName().contains("Demented-Idiot"));
    }

    @Test
    public void testGetInfo() throws Exception {
        assertEquals(7, instance.getInfo().size());
    }

    @Test
    public void testGetLicensedComponents() throws Exception {
        final List<LicensedComponent> licensedComponents = instance.getLicensedComponents();
        assertEquals(3, licensedComponents.size());
        assertEquals("DMDirc", licensedComponents.get(0).getName());
        assertEquals("Plugin1", licensedComponents.get(1).getName());
        assertEquals("Plugin2", licensedComponents.get(2).getName());
        assertEquals(1, licensedComponents.get(0).getLicences().size());
        assertEquals("dmdirc", licensedComponents.get(0).getLicences().get(0).getComponent());
        assertEquals("license4", licensedComponents.get(0).getLicences().get(0).getName());
        assertEquals("License4-Body", licensedComponents.get(0).getLicences().get(0).getBody());
        assertEquals(2, licensedComponents.get(1).getLicences().size());
        assertEquals("component1", licensedComponents.get(1).getLicences().get(0).getComponent());
        assertEquals("license1", licensedComponents.get(1).getLicences().get(0).getName());
        assertEquals("License1-Body", licensedComponents.get(1).getLicences().get(0).getBody());
        assertEquals("component2", licensedComponents.get(1).getLicences().get(1).getComponent());
        assertEquals("license2", licensedComponents.get(1).getLicences().get(1).getName());
        assertEquals("License2-Body", licensedComponents.get(1).getLicences().get(1).getBody());
        assertEquals(1, licensedComponents.get(2).getLicences().size());
        assertEquals("component3", licensedComponents.get(2).getLicences().get(0).getComponent());
        assertEquals("license3", licensedComponents.get(2).getLicences().get(0).getName());
        assertEquals("License3-Body", licensedComponents.get(2).getLicences().get(0).getBody());
    }
}