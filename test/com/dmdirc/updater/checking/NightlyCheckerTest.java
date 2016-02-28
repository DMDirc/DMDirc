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

import com.dmdirc.config.ConfigBinder;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.Version;
import com.dmdirc.util.io.Downloader;
import com.dmdirc.util.io.FileUtils;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NightlyCheckerTest {

    private NightlyChecker instance;
    @Mock private AggregateConfigProvider config;
    @Mock private Downloader downloader;
    @Mock private ConfigBinder configBinder;
    @Mock private UpdateComponent componentOne;
    @Mock private UpdateComponent componentTwo;
    @Mock private UpdateComponent componentThree;
    @Mock private UpdateComponent componentFour;

    @Before
    public void setUp() throws Exception {
        when(config.getBinder()).thenReturn(configBinder);
        when(downloader.getPage(anyString())).thenReturn(
                Files.readAllLines(
                        FileUtils.getPathForResource(getClass().getResource("nightlies.json"))));
        when(componentOne.getName()).thenReturn("ui_swing");
        when(componentTwo.getName()).thenReturn("client");
        when(componentThree.getName()).thenReturn("time");
        when(componentFour.getName()).thenReturn("audio");
        when(componentOne.getVersion()).thenReturn(new Version("1.2"));
        when(componentTwo.getVersion()).thenReturn(new Version("4.5"));
        when(componentThree.getVersion()).thenReturn(new Version("0.1"));
        when(componentFour.getVersion()).thenReturn(new Version("10"));
        instance = new NightlyChecker(config, downloader);
        instance.setChannel("NIGHTLY");
    }

    @Test
    public void testUnknownChannel() throws Exception {
        instance.setChannel("RANDOM");
        final Map<UpdateComponent, UpdateCheckResult> updates =
                instance.checkForUpdates(Lists.newArrayList(componentThree));
        assertEquals(0, updates.size());
    }

    @Test
    public void testNotNightly() throws Exception {
        instance.setChannel("STABLE");
        final Map<UpdateComponent, UpdateCheckResult> updates =
                instance.checkForUpdates(Lists.newArrayList(componentThree));
        assertEquals(0, updates.size());
    }

    @Test
    public void testNightly() throws Exception {
        final Map<UpdateComponent, UpdateCheckResult> updates =
                instance.checkForUpdates(Lists.newArrayList(componentThree));
        assertEquals(1, updates.size());
    }

    @Test
    public void testNoUpdates() throws Exception {
        final Map<UpdateComponent, UpdateCheckResult> updates = instance.checkForUpdates(
                Lists.newArrayList(componentOne, componentTwo));
        assertEquals(0, updates.size());
    }

    @Test
    public void testOneUpdate() throws Exception {
        final Map<UpdateComponent, UpdateCheckResult> updates = instance.checkForUpdates(
                Lists.newArrayList(componentOne, componentThree));
        assertEquals(1, updates.size());
    }

    @Test
    public void testMultipleUpdate() throws Exception {
        final Map<UpdateComponent, UpdateCheckResult> updates = instance.checkForUpdates(
                Lists.newArrayList(componentOne, componentTwo, componentThree, componentFour));
        assertEquals(2, updates.size());
    }

    @Test
    public void testMalformedPage() throws Exception {
        when(downloader.getPage(anyString())).thenThrow(new IOException("Failure."));
        final Map<UpdateComponent, UpdateCheckResult> updates =
                instance.checkForUpdates(Lists.newArrayList(componentThree));
        assertEquals(0, updates.size());
    }
}
