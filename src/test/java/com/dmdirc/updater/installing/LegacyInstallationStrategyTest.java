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

package com.dmdirc.updater.installing;

import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.retrieving.SingleFileRetrievalResult;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LegacyInstallationStrategyTest {

    private LegacyInstallationStrategy strategy;

    private UpdateComponent component;

    private SingleFileRetrievalResult result;

    private UpdateInstallationListener listener;

    @Before
    public void setup() {
        this.strategy = new LegacyInstallationStrategy();
        this.component = mock(UpdateComponent.class);
        this.result = mock(SingleFileRetrievalResult.class);
        this.listener = mock(UpdateInstallationListener.class);
    }

    @Test
    public void testCallsInstallWithCorrectPath() throws Exception { // NOPMD
        final Path file = mock(Path.class);
        final Path absoluteFile = mock(Path.class);
        when(file.toString()).thenReturn("path");
        when(file.toAbsolutePath()).thenReturn(absoluteFile);
        when(absoluteFile.toString()).thenReturn("/my/path");
        when(result.getFile()).thenReturn(file);
        strategy.installImpl(component, result);
        verify(component).doInstall(file);
    }

    @Test
    public void testRaisesCompletedEvent() throws Exception { // NOPMD
        final Path file = mock(Path.class);
        final Path absoluteFile = mock(Path.class);
        when(file.toString()).thenReturn("path");
        when(file.toAbsolutePath()).thenReturn(absoluteFile);
        when(absoluteFile.toString()).thenReturn("/my/path");
        strategy.addUpdateInstallationListener(listener);
        strategy.installImpl(component, result);
        verify(listener).installCompleted(component);
    }

    @Test
    public void testRaisesFailedEvent() throws Exception { // NOPMD
        final Path file = mock(Path.class);
        final Path absoluteFile = mock(Path.class);
        when(file.toString()).thenReturn("path");
        when(file.toAbsolutePath()).thenReturn(absoluteFile);
        when(absoluteFile.toString()).thenReturn("/my/path");
        when(component.doInstall(any())).thenThrow(new IOException());
        strategy.addUpdateInstallationListener(listener);
        strategy.installImpl(component, result);
        verify(listener).installFailed(component);
    }

}
