/*
 * Copyright (c) 2006-2017 DMDirc Developers
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

package com.dmdirc.plugins;

import com.dmdirc.tests.JimFsRule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(MockitoJUnitRunner.class)
public class PluginFileHandlerTest {

    @Rule public final JimFsRule jimFsRule = new JimFsRule();

    @Mock private PluginManager pluginManager;

    private PluginFileHandler fileHandler;
    private Path directory;

    @Before
    public void setUp() throws IOException {
        directory = jimFsRule.getPath("plugins");

        Files.createDirectory(directory);
        fileHandler = new PluginFileHandler(directory);
    }

    @Test
    public void testMovesUpdatedJar() throws IOException {
        Files.copy(getClass().getResource("plugin.jar").openStream(),
                directory.resolve("plugin.jar.update"));

        fileHandler.refresh(pluginManager);

        assertFalse(Files.exists(directory.resolve("plugin.jar.update")));
        assertTrue(Files.exists(directory.resolve("plugin.jar")));
    }

    @Test
    public void testReplacesExistingJarWithUpdatedVersion() throws IOException {
        Files.write(directory.resolve("plugin.jar"), new byte[0]);
        Files.copy(getClass().getResource("plugin.jar").openStream(),
                directory.resolve("plugin.jar.update"));

        assumeTrue(Files.size(directory.resolve("plugin.jar")) == 0);

        fileHandler.refresh(pluginManager);

        assertFalse(Files.exists(directory.resolve("plugin.jar.update")));
        assertTrue(Files.exists(directory.resolve("plugin.jar")));
        assertTrue(Files.size(directory.resolve("plugin.jar")) > 0);
    }

}