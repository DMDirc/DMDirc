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

package com.dmdirc.config.profiles;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class YamlProfileStoreTest {

    @Test
    public void testReadProfiles() throws Exception {
        final ProfileStore store = new YamlProfileStore(Paths.get(getClass()
                .getResource("profiles.yml").toURI()));
        final Collection<Profile> profiles = store.readProfiles();
        assertEquals(3, profiles.size());
    }

    @Test
    public void testWriteProfiles() throws Exception {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            final ProfileStore store = new YamlProfileStore(fs.getPath("/")
                    .resolve("profiles.yml"));
            final Profile profile1 = new Profile("name", "realname", Optional.of("ident"),
                    Lists.newArrayList("nickname"));
            final Set<Profile> profiles = Sets.newHashSet(profile1);
            store.writeProfiles(profiles);
            final List<String> lines = Files.readAllLines(fs.getPath("/").resolve("profiles.yml"));
            assertEquals(5, lines.size());
        }
    }
}