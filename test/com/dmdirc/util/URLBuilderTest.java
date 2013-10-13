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

package com.dmdirc.util;

import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.PluginMetaData;
import com.dmdirc.ui.themes.ThemeManager;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Provider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class URLBuilderTest {

    @Mock private Provider<PluginManager> pluginManagerProvider;
    @Mock private Provider<ThemeManager> themeManagerProvider;
    @Mock private PluginManager pluginManager;
    @Mock private ThemeManager themeManager;
    @Mock private PluginInfo pluginInfo;
    @Mock private PluginMetaData pluginMetaData;

    @Before
    public void setup() throws MalformedURLException {
        when(pluginManagerProvider.get()).thenReturn(pluginManager);
        when(themeManagerProvider.get()).thenReturn(themeManager);
        when(pluginManager.getPluginInfoByName(Matchers.anyString())).thenReturn(pluginInfo);
        when(themeManager.getDirectory()).thenReturn("/themes/");
        when(pluginInfo.getMetaData()).thenReturn(pluginMetaData);
        when(pluginMetaData.getPluginUrl()).thenReturn(new URL("file://url/testPlugin"));
    }

    @Test
    public void testGetUrlForFileWithoutFilePrefix() throws MalformedURLException {
        URLBuilder urlBuilder = new URLBuilder(pluginManagerProvider, themeManagerProvider);
        Assert.assertEquals(new URL("file://test"), urlBuilder.getUrlForFile("test"));
    }

    @Test
    public void testGetUrlForFileWithFilePrefix() throws MalformedURLException {
        URLBuilder urlBuilder = new URLBuilder(pluginManagerProvider, themeManagerProvider);
        Assert.assertEquals(new URL("file://test"), urlBuilder.getUrlForFile("file://test"));
    }

    @Test
    public void testGetUrlForJarFile() throws MalformedURLException {
        URLBuilder urlBuilder = new URLBuilder(pluginManagerProvider, themeManagerProvider);
        Assert.assertEquals(new URL("jar:file:/jarFile!/test"), urlBuilder.getUrlForJarFile("jarFile", "test"));
    }

    @Test
    public void testGetUrlForDMDircResourceNonExistant() {
        URLBuilder urlBuilder = new URLBuilder(pluginManagerProvider, themeManagerProvider);
        Assert.assertNull(urlBuilder.getUrlForDMDircResource("test"));
    }

    @Test
    public void testGetUrlForDMDircResource() throws MalformedURLException {
        URLBuilder urlBuilder = new URLBuilder(pluginManagerProvider, themeManagerProvider);
        URL url = urlBuilder.getUrlForDMDircResource("com/dmdirc/Main.class");
        Assert.assertEquals("file", url.getProtocol());
        Assert.assertTrue(url.getFile().endsWith("com/dmdirc/Main.class"));
    }

    @Test
    public void testGetUrlForThemeResource() throws MalformedURLException {
        URLBuilder urlBuilder = new URLBuilder(pluginManagerProvider, themeManagerProvider);
        Assert.assertEquals(new URL("jar:file:/themes/testTheme.zip!/testFile"), urlBuilder.getUrlForThemeResource("testTheme", "testFile"));
    }

    @Test
    public void testGetUrlForPluginResource() throws MalformedURLException {
        URLBuilder urlBuilder = new URLBuilder(pluginManagerProvider, themeManagerProvider);
        Assert.assertEquals(new URL("jar:file:/testPlugin!/testFile"), urlBuilder.getUrlForPluginResource("testPlugin", "testFile"));
    }

    @Test
    public void testGetUrlDMDirc() {
        URLBuilder urlBuilder = new URLBuilder(pluginManagerProvider, themeManagerProvider);
        URL url = urlBuilder.getUrl("dmdirc://com/dmdirc/Main.class");
        Assert.assertEquals("file", url.getProtocol());
        Assert.assertTrue(url.getFile().endsWith("com/dmdirc/Main.class"));
    }

    @Test
    public void testGetUrlJar() throws MalformedURLException {
        URLBuilder urlBuilder = new URLBuilder(pluginManagerProvider, themeManagerProvider);
        Assert.assertEquals(new URL("jar:file:/jarFile!/testFile"), urlBuilder.getUrl("jar://jarFile:testFile"));
    }

    @Test
    public void testGetUrlZip() throws MalformedURLException {
        URLBuilder urlBuilder = new URLBuilder(pluginManagerProvider, themeManagerProvider);
        Assert.assertEquals(new URL("jar:file:/zipFile!/testFile"), urlBuilder.getUrl("zip://zipFile:testFile"));
    }

    @Test
    public void testGetUrlPlugin() throws MalformedURLException {
        URLBuilder urlBuilder = new URLBuilder(pluginManagerProvider, themeManagerProvider);
        Assert.assertEquals(new URL("jar:file:/testPlugin!/testFile"), urlBuilder.getUrl("plugin://pluginFile:testFile"));
    }

    @Test
    public void testGetUrlTheme() throws MalformedURLException {
        URLBuilder urlBuilder = new URLBuilder(pluginManagerProvider, themeManagerProvider);
        Assert.assertEquals(new URL("jar:file:/themes/themeFile.zip!/testFile"), urlBuilder.getUrl("theme://themeFile:testFile"));
    }

    @Test
    public void testGetUrlHTTP() throws MalformedURLException {
        URLBuilder urlBuilder = new URLBuilder(pluginManagerProvider, themeManagerProvider);
        Assert.assertEquals(new URL("http://testDomain"), urlBuilder.getUrl("http://testDomain"));
    }

    @Test
    public void testGetUrlFileWithPrefix() throws MalformedURLException {
        URLBuilder urlBuilder = new URLBuilder(pluginManagerProvider, themeManagerProvider);
        Assert.assertEquals(new URL("file://testFile"), urlBuilder.getUrl("file://testFile"));
    }

    @Test
    public void testGetUrlFileWithoutPrefix() throws MalformedURLException {
        URLBuilder urlBuilder = new URLBuilder(pluginManagerProvider, themeManagerProvider);
        Assert.assertEquals(new URL("file://testFile"), urlBuilder.getUrl("testFile"));
    }
}
