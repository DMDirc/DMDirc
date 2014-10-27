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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class YamlProfileStoreTest {

    private Profile profile1;
    private Profile profile2;
    private Profile profile3;

    @Before
    public void setup() {
        profile1 = new Profile("profile1", "realname1", Optional.empty(),
                Lists.newArrayList("nickname1", "nickname2"));
        profile2 = new Profile("profile2", "realname2", Optional.of("ident2"),
                Lists.newArrayList("nickname1", "nickname3"));
        profile3 = new Profile("profile3", "realname3", Optional.empty(),
                Lists.newArrayList("nickname3"));
    }

    @Test
    public void testReadProfiles() throws Exception {
        final ProfileStore store = new YamlProfileStore(Paths.get(getClass()
                .getResource("profiles.yml").toURI()));
        final Collection<Profile> profiles = store.readProfiles();
        assertEquals(3, profiles.size());
        profiles.forEach(System.out::println);
        assertTrue(profiles.contains(profile1));
        assertTrue(profiles.contains(profile2));
        assertTrue(profiles.contains(profile3));
    }

    @Test
    public void testWriteProfiles() throws Exception {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            final ProfileStore store = new YamlProfileStore(fs.getPath("/")
                    .resolve("profiles.yml"));
            final List<Profile> profiles = Lists.newArrayList(profile1, profile2, profile3);
            store.writeProfiles(profiles);
            final Collection<Profile> readProfiles = store.readProfiles();
            assertEquals(3, readProfiles.size());
            assertTrue(readProfiles.contains(profile1));
            assertTrue(readProfiles.contains(profile2));
            assertTrue(readProfiles.contains(profile3));
        }
    }
}