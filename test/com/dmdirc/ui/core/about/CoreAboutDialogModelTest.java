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
import com.dmdirc.util.ClientInfo;

import java.nio.file.Path;

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
    private CoreAboutDialogModel instance;

    @Before
    public void setUp() throws Exception {
        when(clientInfo.getVersionInformation()).thenReturn("DMDirc Version");
        when(clientInfo.getOperatingSystemInformation()).thenReturn("OS Version");
        when(clientInfo.getJavaInformation()).thenReturn("Java Version");
        when(globalConfig.getOption("identity", "modealiasversion")).thenReturn("ModeAlias Version");
        instance = new CoreAboutDialogModel(globalConfig, path, clientInfo, eventBus);
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
        assertEquals(0, instance.getLicensedComponents().size());
    }
}