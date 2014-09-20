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

package com.dmdirc.config;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.interfaces.config.ConfigProvider;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class IdentityManagerTest {

    @Mock private DMDircMBassador eventBus;

    private Path baseDirectory;
    private Path identitiesDirectory;

    @Before
    @SuppressWarnings("resource")
    public void setUp() throws Exception {
        final FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        baseDirectory = fs.getPath("config");
        identitiesDirectory = baseDirectory.resolve("identities");
    }

    @Test
    public void testLoadsVersionIdentity() throws InvalidIdentityFileException {
        final IdentityManager identityManager = new IdentityManager(
                baseDirectory, identitiesDirectory, eventBus);
        identityManager.initialise();

        final ConfigProvider versionSettings = identityManager.getVersionSettings();
        assertNotNull(versionSettings);
        assertEquals(ConfigTarget.TYPE.GLOBALDEFAULT, versionSettings.getTarget().getType());
        assertEquals("DMDirc version information", versionSettings.getName());
    }

    @Test
    public void testRenamesExistingDefaultsFile() throws IOException, InvalidIdentityFileException {
        Files.createDirectories(identitiesDirectory);
        Files.createFile(identitiesDirectory.resolve("default"));

        final IdentityManager identityManager = new IdentityManager(
                baseDirectory, identitiesDirectory, eventBus);
        identityManager.initialise();

        assertTrue(Files.exists(identitiesDirectory.resolve("default.old")));
        assertTrue(Files.isDirectory(identitiesDirectory.resolve("default")));
    }

    @Test
    public void testRenamesExistingDefaultsFileWithSuffix() throws IOException,
            InvalidIdentityFileException {
        Files.createDirectories(identitiesDirectory);
        Files.createFile(identitiesDirectory.resolve("default"));
        Files.createFile(identitiesDirectory.resolve("default.old"));
        Files.createFile(identitiesDirectory.resolve("default.old-1"));

        final IdentityManager identityManager = new IdentityManager(
                baseDirectory, identitiesDirectory, eventBus);
        identityManager.initialise();

        assertTrue(Files.exists(identitiesDirectory.resolve("default.old")));
        assertTrue(Files.exists(identitiesDirectory.resolve("default.old-1")));
        assertTrue(Files.exists(identitiesDirectory.resolve("default.old-2")));
        assertTrue(Files.isDirectory(identitiesDirectory.resolve("default")));
    }

    @Test
    public void testExtractsDefaults() throws InvalidIdentityFileException {
        final IdentityManager identityManager = new IdentityManager(
                baseDirectory, identitiesDirectory, eventBus);
        identityManager.initialise();

        assertTrue(Files.isDirectory(identitiesDirectory.resolve("default")));
        assertTrue(Files.exists(identitiesDirectory.resolve("default").resolve("defaults")));
        assertTrue(Files.exists(identitiesDirectory.resolve("default").resolve("formatter")));
    }

    @Test
    public void testAlwaysUpdatesFormatter() throws InvalidIdentityFileException, IOException {
        final Path formatterPath = identitiesDirectory.resolve("default").resolve("formatter");

        Files.createDirectories(identitiesDirectory.resolve("default"));
        Files.createFile(formatterPath);

        final IdentityManager identityManager = new IdentityManager(
                baseDirectory, identitiesDirectory, eventBus);
        identityManager.initialise();

        assertTrue(Files.exists(formatterPath));
        assertTrue(Files.size(formatterPath) > 0);
    }

    @Test
    public void testCreatesDefaultProfile() throws InvalidIdentityFileException {
        final IdentityManager identityManager = new IdentityManager(
                baseDirectory, identitiesDirectory, eventBus);
        identityManager.initialise();

        final List<ConfigProvider> profiles = identityManager.getProvidersByType("profile");
        assertEquals(1, profiles.size());
        assertEquals("Default Profile", profiles.get(0).getName());
    }

    @Test
    public void testUsesSystemUsernameForProfileNickname() throws InvalidIdentityFileException {
        final IdentityManager identityManager = new IdentityManager(
                baseDirectory, identitiesDirectory, eventBus);
        identityManager.initialise();

        System.setProperty("user.name", "Awesome User");
        final ConfigProvider config = identityManager.createProfileConfig("test 123");
        assertEquals("Awesome_User", config.getOption("profile", "nicknames"));
    }

    @Test
    public void testDoesNotCreateProfileIfOneExists()
            throws IOException, InvalidIdentityFileException {
        Files.createDirectories(identitiesDirectory);
        Files.copy(getClass().getResourceAsStream("profile-new"),
                identitiesDirectory.resolve("profile"));

        final IdentityManager identityManager = new IdentityManager(
                baseDirectory, identitiesDirectory, eventBus);
        identityManager.initialise();

        final List<ConfigProvider> profiles = identityManager.getProvidersByType("profile");
        assertEquals(1, profiles.size());
        assertEquals("New Profile", profiles.get(0).getName());
    }

}