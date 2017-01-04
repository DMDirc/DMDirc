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

package com.dmdirc.ui.core.about;

import com.dmdirc.commandline.CommandLineParser;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.PluginMetaData;
import com.dmdirc.util.ClientInfo;
import com.dmdirc.util.io.FileUtils;

import com.google.common.collect.Lists;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
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
    @Mock private Path path;
    @Mock private EventBus eventBus;
    @Mock private PluginManager pluginManager;
    @Mock private PluginMetaData pluginMetaData1;
    @Mock private PluginMetaData pluginMetaData2;
    @Mock private PluginInfo pluginInfo1;
    @Mock private PluginInfo pluginInfo2;
    @Mock private CommandLineParser commandLineParser;
    private CoreAboutDialogModel instance;

    @Before
    public void setUp() throws Exception {
        final Path pluginPath1 = FileUtils.getPathForResource(getClass().getResource("license1"));
        final Path pluginPath2 = FileUtils.getPathForResource(getClass().getResource("license2"));
        when(clientInfo.getVersionInformation()).thenReturn("DMDirc Version");
        when(clientInfo.getOperatingSystemInformation()).thenReturn("OS Version");
        when(clientInfo.getJavaInformation()).thenReturn("Java Version");
        when(pluginManager.getPluginInfos()).thenReturn(Lists.newArrayList(pluginInfo1, pluginInfo2));
        when(pluginInfo1.getPath("/META-INF/licences/")).thenReturn(pluginPath1);
        when(pluginInfo2.getPath("/META-INF/licences/")).thenReturn(pluginPath2);
        when(pluginInfo1.getMetaData()).thenReturn(pluginMetaData1);
        when(pluginInfo2.getMetaData()).thenReturn(pluginMetaData2);
        when(pluginMetaData1.getFriendlyName()).thenReturn("Plugin1");
        when(pluginMetaData2.getFriendlyName()).thenReturn("Plugin2");
        when(commandLineParser.getLauncherVersion()).thenReturn(Optional.of("Unknown"));
        instance = new CoreAboutDialogModel(path, clientInfo, eventBus, pluginManager,
                commandLineParser);
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
        final Developer MD87 = Developer.create("Chris 'MD87' Smith",
                "https://www.md87.co.uk");
        final Developer Greboid = Developer.create("Gregory 'Greboid' Holmes",
                "https://www.greboid.com");
        final Developer Dataforce = Developer.create("Shane 'Dataforce' Mc Cormack",
                "http://home.dataforce.org.uk");

        assertEquals(3, instance.getMainDevelopers().size());
        assertTrue(instance.getMainDevelopers().contains(MD87));
        assertTrue(instance.getMainDevelopers().contains(Greboid));
        assertTrue(instance.getMainDevelopers().contains(Dataforce));
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
    @Ignore("Flakey")
    public void testGetLicensedComponents() throws Exception {
        final Licence dmdircLicense = Licence.create("dmdirc", "license4", "License4-Body");
        final Licence pligin1Licence1 = Licence.create("component1", "license1", "License1-Body");
        final Licence plugin1Licence2 = Licence.create("component2", "license2", "License2-Body");
        final Licence plugin2Licence2 = Licence.create("component3", "license3", "License3-Body");
        final LicensedComponent dmdirc = LicensedComponent.create("DMDirc",
                Lists.newArrayList(dmdircLicense));
        final LicensedComponent plugin1 = LicensedComponent.create("Plugin1",
                Lists.newArrayList(pligin1Licence1, plugin1Licence2));
        final LicensedComponent plugin2 = LicensedComponent.create("Plugin2",
                Lists.newArrayList(plugin2Licence2));

        final List<LicensedComponent> licensedComponents = instance.getLicensedComponents();
        assertEquals(3, licensedComponents.size());
        assertTrue(licensedComponents.contains(dmdirc));
        assertTrue(licensedComponents.contains(plugin1));
        assertTrue(licensedComponents.contains(plugin2));
    }
}
