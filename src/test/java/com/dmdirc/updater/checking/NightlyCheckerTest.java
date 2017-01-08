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

import com.dmdirc.config.binding.ConfigBinder;
import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.Version;
import com.dmdirc.util.io.Downloader;
import com.dmdirc.util.io.FileUtils;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NightlyCheckerTest {

    private NightlyChecker instance;
    @Mock private AggregateConfigProvider config;
    @Mock private Downloader downloader;
    @Mock private ConfigBinder configBinder;
    @Mock private UpdateComponent uiswingUpdateComponent;
    @Mock private UpdateComponent clientUpdateComponent;
    @Mock private UpdateComponent timeUpdateComponent;
    @Mock private UpdateComponent audioUpdateComponent;
    @Mock private UpdateComponent uiweb2UpdateComponent;
    @Mock private UpdateComponent randomtestUpdateComponent;

    @Before
    public void setUp() throws Exception {
        when(config.getBinder()).thenReturn(configBinder);
        when(downloader.getPage(anyString())).thenReturn(
                Files.readAllLines(
                        FileUtils.getPathForResource(getClass().getResource("nightlies.json"))));
        when(uiswingUpdateComponent.getName()).thenReturn("ui_swing");
        when(clientUpdateComponent.getName()).thenReturn("client");
        when(timeUpdateComponent.getName()).thenReturn("time");
        when(audioUpdateComponent.getName()).thenReturn("audio");
        when(uiweb2UpdateComponent.getName()).thenReturn("ui_web2");
        when(randomtestUpdateComponent.getName()).thenReturn("random-test");
        when(uiswingUpdateComponent.getVersion()).thenReturn(new Version("1.2"));
        when(clientUpdateComponent.getVersion()).thenReturn(new Version("4.5"));
        when(timeUpdateComponent.getVersion()).thenReturn(new Version("0.1"));
        when(audioUpdateComponent.getVersion()).thenReturn(new Version("0.1"));
        when(uiweb2UpdateComponent.getVersion()).thenReturn(new Version("10.0"));
        when(randomtestUpdateComponent.getVersion()).thenReturn(new Version("10.0"));
        instance = new NightlyChecker(config, downloader);
        instance.setChannel("NIGHTLY");
    }

    @Test
    public void testUnknownChannel() throws Exception {
        instance.setChannel("RANDOM");
        final Map<UpdateComponent, UpdateCheckResult> updates =
                instance.checkForUpdates(Lists.newArrayList(timeUpdateComponent));
        assertEquals(0, updates.size());
    }

    @Test
    public void testNotNightly() throws Exception {
        instance.setChannel("STABLE");
        final Map<UpdateComponent, UpdateCheckResult> updates =
                instance.checkForUpdates(Lists.newArrayList(timeUpdateComponent));
        assertEquals(0, updates.size());
    }

    @Test
    public void testNightly() throws Exception {
        final Map<UpdateComponent, UpdateCheckResult> updates =
                instance.checkForUpdates(Lists.newArrayList(timeUpdateComponent));
        assertEquals(1, updates.size());
    }

    @Test
    public void testNoUpdates() throws Exception {
        final Map<UpdateComponent, UpdateCheckResult> updates = instance.checkForUpdates(
                Lists.newArrayList(uiswingUpdateComponent, clientUpdateComponent));
        assertEquals(0, updates.size());
    }

    @Test
    public void testOneUpdate() throws Exception {
        final Map<UpdateComponent, UpdateCheckResult> updates = instance.checkForUpdates(
                Lists.newArrayList(uiswingUpdateComponent, timeUpdateComponent));
        assertEquals(1, updates.size());
    }

    @Test
    public void testMultipleUpdate() throws Exception {
        final Map<UpdateComponent, UpdateCheckResult> updates = instance.checkForUpdates(
                Lists.newArrayList(uiswingUpdateComponent, clientUpdateComponent,
                        timeUpdateComponent, audioUpdateComponent));
        assertEquals(2, updates.size());
    }

    @Test
    public void testMalformedPage() throws Exception {
        when(downloader.getPage(anyString())).thenThrow(new IOException("Failure."));
        final Map<UpdateComponent, UpdateCheckResult> updates =
                instance.checkForUpdates(Lists.newArrayList(timeUpdateComponent));
        assertEquals(0, updates.size());
    }

    @Test
    public void testNames() throws Exception {
        when(downloader.getPage(anyString())).thenReturn(
                Files.readAllLines(
                        FileUtils.getPathForResource(getClass().getResource("nightlies2.json"))));
        final Map<UpdateComponent, UpdateCheckResult> updates =
                instance.checkForUpdates(Lists.newArrayList(uiswingUpdateComponent,
                        clientUpdateComponent, timeUpdateComponent, audioUpdateComponent,
                        randomtestUpdateComponent, uiweb2UpdateComponent));
        final List<String> updateNames = updates.keySet().stream()
                .map(UpdateComponent::getName)
                .collect(Collectors.toList());
        assertTrue(updateNames.contains("client"));
        assertTrue(updateNames.contains("time"));
        assertTrue(updateNames.contains("ui_swing"));
        assertTrue(updateNames.contains("ui_web2"));
        assertTrue(updateNames.contains("random-test"));
        assertTrue(updateNames.contains("audio"));
    }
}
