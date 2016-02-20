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

package com.dmdirc.config;

import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.tests.JimFsRule;
import com.dmdirc.util.ClientInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class IdentityManagerTest {

    @Rule public final JimFsRule jimFsRule = new JimFsRule();

    @Mock private ClientInfo clientInfo;

    private Path baseDirectory;
    private Path identitiesDirectory;

    @Before
    public void setUp() throws Exception {
        baseDirectory = jimFsRule.getPath("config");
        identitiesDirectory = baseDirectory.resolve("identities");
    }

    @Test
    public void testLoadsVersionIdentity() throws InvalidIdentityFileException {
        final IdentityManager identityManager = new IdentityManager(
                baseDirectory, identitiesDirectory, clientInfo);
        identityManager.initialise();

        final ConfigProvider versionSettings = identityManager.getVersionSettings();
        assertNotNull(versionSettings);
        assertEquals(ConfigTarget.TYPE.GLOBALDEFAULT, versionSettings.getTarget().getType());
        assertEquals("DMDirc version information", versionSettings.getName());
    }

    @Test
    public void testUsesSystemUsernameForProfileNickname() throws InvalidIdentityFileException {
        final IdentityManager identityManager = new IdentityManager(
                baseDirectory, identitiesDirectory, clientInfo);
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
                baseDirectory, identitiesDirectory, clientInfo);
        identityManager.initialise();

        final Collection<ConfigProvider> profiles = identityManager.getProvidersByType("profile");
        assertEquals(1, profiles.size());
        assertEquals("New Profile", profiles.stream().findAny().get().getName());
    }

}